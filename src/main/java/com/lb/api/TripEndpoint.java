package com.lb.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.lb.ai.models.TripAgentChatModel;
import com.lb.application.TripCoordinator;

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

  @Post("/search/")
  public CompletionStage<String> searchTrip(Question question) {
      TripCoordinator coordinator = new TripCoordinator(tripAgentChatModel, componentClient);
      return coordinator.requestTrip(question.question());
  }

  @Post("/book")
  public CompletionStage<String> book(BookingTripRequest bookingTripRequest) {
    return new TripCoordinator(tripAgentChatModel, componentClient).bookTrip(bookingTripRequest);
  }
}
