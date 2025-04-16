package com.lb.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.lb.application.TripCoordinator;
import com.lb.ai.models.TripAgentChatModel;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/trip")
public class TripEndpoint {

  private final ComponentClient componentClient;
  private final TripAgentChatModel tripAgentChatModel;

  public TripEndpoint(ComponentClient componentClient, TripAgentChatModel tripAgentChatModel) {
    this.componentClient = componentClient;
    this.tripAgentChatModel = tripAgentChatModel;
  }

  // TODO make a GET with a string explaining the constraints
  @Post("/search/")
  public CompletionStage<String> searchTrip(Question question) {
    return new TripCoordinator(tripAgentChatModel, componentClient).requestTrip(question.question());
  }

  // TODO make a GET with a string explaining the constraints
  @Post("/book")
  public CompletionStage<String> book(Trip trip) {
    return new TripCoordinator(tripAgentChatModel, componentClient).bookTrip(trip);
  }
}
