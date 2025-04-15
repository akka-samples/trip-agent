package com.lb.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import com.lb.application.models.TripAgentChatModel;
import com.lb.domain.Flight;
import com.lb.domain.FlightConstraints;
import com.lb.domain.TripConstraints;
import com.lb.domain.TripOptions;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.springframework.ai.chat.client.ChatClient;

@ComponentId("trip-agent")
public class TripAgentWorkflow extends Workflow<TripOptions> {

  private final ChatClient chatClient;
  private final ComponentClient componentClient;

  public TripAgentWorkflow(TripAgentChatModel chatModel, ComponentClient componentClient) {
    this.chatClient = ChatClient.create(chatModel);
    this.componentClient = componentClient;
  }

  // TODO use streaming
  // TODO I feel it's better a String that a more defined type, so we can leverage the LLM
  // understanding without bothering the user with
  // TODO model the Workflow as a dynamic agent.
  //    public Effect<Done> findTrip(String constraints){
  //       chatClient
  //               .prompt("You need to find a trip that fullfil this following constraints" +
  // constraints)
  //               .tools(new FlightBookingSpecialistTool(componentClient))
  //
  //       var newState = currentState().addToConversation(response);
  //       return effects().updateState(newState);
  //    }

  public Effect<Done> startSearch(TripConstraints tripConstraints) {

    TripOptions initialState = TripOptions.empty().withDescription(tripConstraints.toString());

    return effects().updateState(initialState).transitionTo("requestFlight").thenReply(Done.done());
  }

  @Override
  public WorkflowDef<TripOptions> definition() {
    Step lookFlight =
        step("requestFlight")
            .asyncCall(FlightConstraints.class, this::findFlightsCommand)
            .andThen(
                (Class) List.class,
                res -> { // TODO review this class solution
                  return effects()
                      .updateState(currentState().withFlights((List<Flight>) res))
                      .transitionTo("requestAccomodation");
                });
    return workflow().addStep(lookFlight);
  }

  private CompletionStage<List<Flight>> findFlightsCommand(FlightConstraints constraints) {
    return componentClient
        .forEventSourcedEntity(String.valueOf(constraints.hashCode()))
        .method(FlightBookingEntity::findFlights)
        .invokeAsync(constraints);
  }
}
