package com.lb.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import com.lb.ai.models.TripAgentChatModel;
import com.lb.ai.tools.*;
import com.lb.domain.Accommodation;
import com.lb.domain.Flight;
import com.lb.domain.TripSearchState;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

@ComponentId("trip-agent")
public class TripAgentWorkflow extends Workflow<TripSearchState> {

  private static final Logger log = LoggerFactory.getLogger(TripAgentWorkflow.class);

  ChatClient chatClient;
  ComponentClient componentClient;

  public TripAgentWorkflow(ComponentClient componentClient, TripAgentChatModel tripAgentChatModel) {
    this.chatClient = ChatClient.create(tripAgentChatModel);
    this.componentClient = componentClient;
  }

  @Override
  public WorkflowDef<TripSearchState> definition() {
    // Step 1 look for flights
    Step searchFlights =
        step("search-flights")
            .call(String.class, this::findFlights) // TODO use async call here and in accommodations
            .andThen(
                FlightAPIResponseList.class,
                flights -> {
                  List<Flight> domainFlights = FlightMapper.mapFlights(flights.flights);
                  storeFlights(domainFlights);
                  return effects()
                      .updateState(currentState().withFlights(domainFlights))
                      .transitionTo("search-accomodations");
                })
            .timeout(Duration.ofSeconds(60));
    // Step 2 look for accommodations (maybe this and the above using async)

    Step searchAccommodations =
        step("search-accommodations")
            .call(String.class, this::findAccommodations)
            .andThen(
                AccommodationAPIResponseList.class,
                accommodations -> {
                  List<Accommodation> domainAccommodations =
                      AccommodationMapper.mapAccommodations(accommodations.accommodations);
                  return effects()
                      .updateState(currentState().withAccommodations(domainAccommodations))
                      .transitionTo("send-mail");
                })
            .timeout(Duration.ofSeconds(60));

    // Step 2 send mail (whether plan succeeds or not
    Step sendMail =
        step("send-mail")
            .call(
                String.class,
                request -> {
                  return sendMail(
                      request,
                      currentState().trip().flights(),
                      currentState().trip().accommodations());
                })
            .andThen(
                Boolean.class,
                __ -> {
                  return effects().end();
                })
            .timeout(Duration.ofSeconds(90));

    // Step 3 deal with errors
    return workflow().addStep(searchFlights).addStep(searchAccommodations).addStep(sendMail);
  }

  public Effect<String> startSearch(String request) {
    // TODO return error directly if not email is provided.
    // TODO? I can add the question as the state of the workflow
    TripSearchState initialState =
        new TripSearchState(TripSearchState.Trip.empty(), TripSearchState.RequestStatus.RECEIVED);
    return effects()
        .updateState(initialState)
        .transitionTo("search-flights", request)
        .thenReply(
            "\"We are processing your request. We'll send you the response to your email in a minute.");
  }

  // TODO move to ai?
  public FlightAPIResponseList findFlights(String question) {
    log.info("looking for flights");
    Flux<String> chatResponseFlights =
        chatClient
            .prompt(
                String.format(
                    """
                       find ONLY flights with the following constraints %s. Ignore any constraints that don't refer flights
                       Parse the flights as a JSON such they fit a schema parseable to a Java class like this:
                       FlightAPIResponse(String id, String from, String to, ZonedDateTime departure, ZonedDateTime arrival, int price)
                       Create the JSON such it has only a list of FlightAPIResponse, do not add any other field
                       """,
                    question))
            .tools(FlightBookingAPITool.getMethodToolCallback("findFlights"))
                .stream()
            .content();
    log.debug("parsing flights: {}", chatResponseFlights);
    var bl = String.join("", Objects.requireNonNull(chatResponseFlights.collectList().block()));
    String onlyFlights = extractJson(bl);
    return new FlightAPIResponseList(
        FlightAPIResponse.extract(new ByteArrayInputStream(onlyFlights.getBytes())));
  }

  private void storeFlights(List<Flight> chatResponseFlights) {

    // load flights into entities
    chatResponseFlights.forEach(
        flight -> {
          componentClient
              .forEventSourcedEntity(flight.id())
              .method(FlightBookingEntity::create)
              .invokeAsync(flight);
        });
  }

  private AccommodationAPIResponseList findAccommodations(String question) {
    String responseAccommodations =
        chatClient
            .prompt(
                String.format(
                    """
                        find ONLY accommodations with the following constraints %s. Ignore any constraints that don't refer accommodations
                        Parse the accommodations as a JSON such they fit a schema parseable to a Java class like this:
                        AccommodationAPIResponse( String id, String name, String neighborhood, ZonedDateTime checkin, ZonedDateTime checkout, int pricepernight)
                        Create the JSON such it has only a list of AccommodationAPIResponse, do not add any other field
                        """,
                    question))
            .tools(AccommodationBookingAPITool.getMethodToolCallback("findAccommodations"))
            .call()
            .content();
    String accommodationsJson = extractJson(responseAccommodations);
    InputStream accommodationStream = new ByteArrayInputStream(accommodationsJson.getBytes());
    List<AccommodationAPIResponse> accommodationAPIResponses =
        AccommodationAPIResponse.extract(accommodationStream);
    return new AccommodationAPIResponseList(accommodationAPIResponses);
  }

  public boolean sendMail(
      String request, List<Flight> flights, List<Accommodation> accommodations) {
    log.info("sending mail");
    String responseMail =
        chatClient
            .prompt(
                String.format(
                    """
                       You are allowed to use the @tool function only once in this conversation. Do not use it more than once, even if more information becomes available
                       Send an email to the email provided in %s, using the requestId provide in %s and the content from %s and %s. The content has flights and accommodations
                       Add in the email a recommendation with the best value combination flight (outbound and return) and accommodation
                       parse the whole content as HTML before sending
                       """,
                    request, request, flights, accommodations))
            .tools(new EmailAPITool())
            .call()
            .content();
    log.debug(String.format("responseMail %s", responseMail));
    return true;
  }

  record AccommodationAPIResponseList(List<AccommodationAPIResponse> accommodations) {}

  public static String extractJson(String response) {
    Pattern pattern = Pattern.compile("\\[.*?]", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(response);
    if (matcher.find()) {
      return matcher.group(0);
    }
    String message = "Could not extract json from response";
    log.error("{}: {}", message, response);
    throw new IllegalArgumentException(message);
  }

  record FlightAPIResponseList(List<FlightAPIResponse> flights) {}
}
