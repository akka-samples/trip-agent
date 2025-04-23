package com.lb.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import com.lb.ai.models.TripAgentChatModel;
import com.lb.ai.tools.*;
import com.lb.domain.Accommodation;
import com.lb.domain.Flight;
import com.lb.domain.TripSearchState;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

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
                      .transitionTo("search-accommodations", currentState().userRequest());
                })
            .timeout(Duration.ofSeconds(60));

    // Step 2 look for accommodations
    Step searchAccommodations =
        step("search-accommodations")
            .call(String.class, this::findAccommodations)
            .andThen(
                AccommodationAPIResponseList.class,
                accommodations -> {
                  List<Accommodation> domainAccommodations =
                      AccommodationMapper.mapAccommodations(accommodations.accommodations);
                  storeAccommodations(domainAccommodations);
                  return effects()
                      .updateState(currentState().withAccommodations(domainAccommodations))
                      .transitionTo("send-mail");
                })
            .timeout(Duration.ofSeconds(60));

    // Step 3 send mail
    Step sendMail =
        step("send-mail")
            .call(
                String.class,
                request -> {
                  return sendMail(
                      currentState().userRequest(),
                      currentState().trip().flights(),
                      currentState().trip().accommodations());
                })
            .andThen(
                Boolean.class,
                __ -> {
                  return effects().end();
                })
            .timeout(Duration.ofSeconds(90));

    Step errorHandler =
        step("error-handler")
            .call(
                () -> {
                  log.error("Trip request failed. Final state {}", currentState());
                  return Done.done();
                })
            .andThen(
                Done.class,
                __ ->
                    effects()
                        .updateState(
                            currentState().withRequestStatus(TripSearchState.RequestStatus.FAILED))
                        .end());

    // Step 3 deal with errors
    return workflow()
        .addStep(searchFlights)
        .addStep(searchAccommodations)
        .addStep(sendMail)
        .failoverTo("error-handler", maxRetries(0));
  }

  public Effect<String> startSearch(String userRequest) {
    TripSearchState initialState =
        new TripSearchState(
            userRequest, TripSearchState.Trip.empty(), TripSearchState.RequestStatus.RECEIVED);
    return effects()
        .updateState(initialState)
        .transitionTo("search-flights", userRequest)
        .thenReply(
            "\"We are processing your userRequest. We'll send you the response to your email in a minute.");
  }

  public FlightAPIResponseList findFlights(String userRequest) {
    log.info("looking for flights");
    return chatClient
        .prompt(
            String.format(
                """
                       find ONLY flights with the following constraints %s. Ignore any constraints that don't refer flights
                       Parse the flights as a JSON such they fit a schema parseable to a Java class like this:
                       FlightAPIResponse(String id, String from, String to, ZonedDateTime departure, ZonedDateTime arrival, int price)
                       Create the JSON such it has only a list of FlightAPIResponse, do not add any other field
                       """,
                userRequest))
        .tools(FlightBookingAPITool.getMethodToolCallback("findFlights"))
        .call()
        .entity(FlightAPIResponseList.class);
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

  private void storeAccommodations(List<Accommodation> chatResponseAccommodations) {
    // load accommodations into entities
    chatResponseAccommodations.forEach(
        accommodation -> {
          componentClient
              .forEventSourcedEntity(accommodation.id())
              .method(AccommodationBookingEntity::create)
              .invokeAsync(accommodation);
        });
  }

  private AccommodationAPIResponseList findAccommodations(String question) {
    log.info("looking for accommodations");
    return chatClient
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
        .entity(AccommodationAPIResponseList.class);
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

  private record FlightAPIResponseList(List<FlightAPIResponse> flights) {}
}
