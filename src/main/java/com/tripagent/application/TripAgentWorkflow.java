package com.tripagent.application;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.tripagent.domain.TripSearchState.StatusTag.*;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import com.tripagent.ai.models.TripAgentChatModel;
import com.tripagent.ai.tools.*;
import com.tripagent.domain.Accommodation;
import com.tripagent.domain.Flight;
import com.tripagent.domain.TripSearchState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

@ComponentId("trip-agent")
public class TripAgentWorkflow extends Workflow<TripSearchState> {

  private static final Logger log = LoggerFactory.getLogger(TripAgentWorkflow.class);

  private final ChatClient chatClient;
  private final ComponentClient componentClient;

  public TripAgentWorkflow(ComponentClient componentClient, TripAgentChatModel tripAgentChatModel) {
    this.chatClient = ChatClient.create(tripAgentChatModel);
    this.componentClient = componentClient;
  }

  @Override
  public WorkflowDef<TripSearchState> definition() {
    // Step 1 look for flights
    Step searchFlights =
        step("search-flights")
            .call(String.class, this::findFlights)
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
                  return effects()
                      .updateState(
                          currentState()
                              .withRequestStatus(
                                  new TripSearchState.RequestStatus(SUCCESSFULLY_FINISHED)))
                      .end();
                })
            .timeout(Duration.ofSeconds(90));

    Step errorHandler =
        step("error-handler")
            .call(
                Exception.class,
                ex -> {
                  log.error("Trip request failed. Current state {}", currentState());
                  return Done.done();
                })
            .andThen(
                Done.class,
                __ ->
                    effects()
                        .updateState(
                            currentState()
                                .withRequestStatus(new TripSearchState.RequestStatus(FAILED)))
                        .end());

    // Step 3 deal with errors
    return workflow()
        .failoverTo("error-handler", maxRetries(0))
        .defaultStepRecoverStrategy(maxRetries(0).failoverTo("error-handler"))
        .addStep(searchFlights)
        .addStep(searchAccommodations)
        .addStep(sendMail)
        .addStep(errorHandler);
  }

  public ReadOnlyEffect<Optional<TripSearchState>> getState() {
    if (currentState() == null) {
      return effects().reply(Optional.empty());
    } else {
      return effects().reply(Optional.of(currentState()));
    }
  }

  public Effect<String> startSearch(String userRequest) {
    TripSearchState initialState =
        new TripSearchState(
            userRequest, TripSearchState.Trip.empty(), new TripSearchState.RequestStatus(STARTED));
    return effects()
        .updateState(initialState)
        .transitionTo("search-flights", userRequest)
        .thenReply(
            "We are processing your Request. We'll send you the response to your email in a minute. Your process id is: "
                + commandContext().workflowId());
  }

  private FlightAPIResponseList findFlights(String userRequest) {
    log.info("looking for flights");
    return chatClient
        .prompt(
            String.format(
                """
                   find ONLY flights within the following constraints %s. Ignore any constraints that don't refer flights
                   If some error shows in the tool you are using do not provide any flights.
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
              .invoke(flight);
        });
  }

  private AccommodationAPIResponseList findAccommodations(String question) {
    log.info("looking for accommodations");
    return chatClient
        .prompt(
            String.format(
                """
                   find ONLY accommodations within the following constraints %s. Ignore any constraints that don't refer accommodations
                   If some error shows in the tool you are using do not provide any accommodations.
                   """,
                question))
        .tools(AccommodationBookingAPITool.getMethodToolCallback("findAccommodations"))
        .call()
        .entity(AccommodationAPIResponseList.class);
  }

  private void storeAccommodations(List<Accommodation> chatResponseAccommodations) {
    // load accommodations into entities
    chatResponseAccommodations.forEach(
        accommodation -> {
          componentClient
              .forEventSourcedEntity(accommodation.id())
              .method(AccommodationBookingEntity::create)
              .invoke(accommodation);
        });
  }

  private boolean sendMail(
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

  private record AccommodationAPIResponseList(List<AccommodationAPIResponse> accommodations) {}

  private record FlightAPIResponseList(List<FlightAPIResponse> flights) {
    static FlightAPIResponseList empty() {
      return new FlightAPIResponseList(List.of());
    }
  }
}
