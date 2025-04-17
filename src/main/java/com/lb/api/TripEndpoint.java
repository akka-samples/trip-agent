package com.lb.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.lb.ai.models.TripAgentChatModel;
import com.lb.ai.tools.FlightBookingAPITool;
import com.lb.application.TripCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/trip")
public class TripEndpoint {

  private final ComponentClient componentClient;
  private final TripAgentChatModel tripAgentChatModel;
  private static final Logger log = LoggerFactory.getLogger(TripEndpoint.class);

  public TripEndpoint(ComponentClient componentClient, TripAgentChatModel tripAgentChatModel) {
    this.componentClient = componentClient;
    this.tripAgentChatModel = tripAgentChatModel;
  }

  @Post("/search/")
  public String searchTrip(Question question) {
    CompletableFuture.runAsync(() -> {
      try {
        TripCoordinator coordinator = new TripCoordinator(tripAgentChatModel, componentClient);
        coordinator.requestTrip(question.question());
      } catch (Exception e) {
        log.error("Error processing request {}.", question.question().hashCode(), e);
      }
    });
    return "Request received. We'll sent an email shortly.";
  }

  @Post("/book")
  public CompletionStage<String> book(BookingTripRequest bookingTripRequest) {
    //WIP
    return new TripCoordinator(tripAgentChatModel, componentClient).bookTrip(bookingTripRequest);
  }
  public record BookingTripRequest(String flightRef, String accommodationRef) {}
  public record Question(String question) {}


}


