package com.lb.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.lb.application.coordinator.TripCoordinator;
import com.lb.application.models.FlightAgentChatModel;

import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/trip")
public class TripEndpoint {

  private final ComponentClient componentClient;
  private final FlightAgentChatModel flightAgentChatModel;

  public TripEndpoint(ComponentClient componentClient, FlightAgentChatModel flightAgentChatModel) {
    this.componentClient = componentClient;
    this.flightAgentChatModel = flightAgentChatModel;
  }

  // TODO make a GET with a string explaining the constraints
  @Post("/search/")
  public CompletionStage<String> searchTrip(Search search) {
    return new TripCoordinator(flightAgentChatModel, componentClient)
        .requestTrip(
            search.id(),
            search.from(),
            search.to(),
            search.locationFrom(),
            search.locationTo(),
            search.flightMaxPrice(),
            search.neighborhood(),
            search.accMaxPrice(),
            search.email());
  }

  // TODO make a GET with a string explaining the constraints
  @Post("/book")
  public CompletionStage<String> book(Trip trip) {
    return new TripCoordinator(flightAgentChatModel, componentClient).bookTrip(trip);
  }
}
