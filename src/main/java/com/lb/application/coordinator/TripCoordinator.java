package com.lb.application.coordinator;

import akka.javasdk.client.ComponentClient;
import com.lb.api.*;
import com.lb.application.FlightBookingEntity;
import com.lb.application.models.FlightAgentChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.io.ByteArrayInputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TripCoordinator {

  // maybe later create an LLM in the first place
  // that use as tooling some other LLMS

  private static final Logger log = LoggerFactory.getLogger(TripCoordinator.class);

  private final FlightAgentChatModel flightAgentChatModel;
  private final ComponentClient componentClient;
  private final ChatClient chatClient;

  public TripCoordinator(
      FlightAgentChatModel flightAgentChatModel, ComponentClient componentClient) {
    this.flightAgentChatModel = flightAgentChatModel;
    this.componentClient = componentClient;
    this.chatClient = ChatClient.create(flightAgentChatModel);
  }

  // Create queries to see top destinations
  // TODO use plain language
  public CompletionStage<String> requestTrip(
      String requestId,
      ZonedDateTime fromDate,
      ZonedDateTime toDate,
      String fromWhere,
      String toWhere,
      int maxPriceFlight,
      String neighborhood,
      int totalPriceAccommodation,
      String email) {
    // TODO use virtual threads
    // TODO Check out why when using CompletableFuture Jackson gets hijacked and parsing fails.
    log.info("looking for flights");

    String responseFlights =
        chatClient
            .prompt(
                String.format(
                    "find flights with the following constraints and parse it as a json: fromDate %s, toDate %s, from %s , to %s, maxPrice < %d",
                    fromDate, toDate, fromWhere, toWhere, maxPriceFlight))
            .tools(new FlightBookingAPITool())
            .call()
            .content();

    log.info("responseFlights: {}", responseFlights);
    // request flights
    String onlyFlights = extractJson(responseFlights);
    List<FlightAPIResponse> flightAPIResponses =
        FlightAPIResponse.extract(new ByteArrayInputStream(onlyFlights.getBytes()));
    log.info("only flights: {}", flightAPIResponses);
    // load flights into entities
    flightAPIResponses.forEach(
        flight -> {
          componentClient.forEventSourcedEntity(flight.id()).method(FlightBookingEntity::create);
        });

    // request accomodations
    String responseAccommodations =
        chatClient
            .prompt(
                String.format(
                    "find accommodations with the following constraints and parse it as a json: fromDate %s, toDate %s, city %s , neighborhood %s, total price for the stay < %d",
                    fromDate, toDate, toWhere, neighborhood, totalPriceAccommodation))
            .tools(new AccommodationBookingAPITool(componentClient))
            .call()
            .content();

    // load accomodatoins into entities
    log.info("responseAccommodations: {}", responseAccommodations);
    // send mail

    String responseMail =
        chatClient
            .prompt(
                String.format(
                    "Send mail to %s, using the requestId %s and the content from %s and %s",
                    email, requestId, responseFlights, responseAccommodations))
            .tools(new EmailAPITool())
            .call()
            .content();
    log.info(String.format("responseMail %s", responseMail));

    // if get options for both
    return CompletableFuture.completedFuture(
        "We are processing your request. We'll send you the response to your email in a minute.");
  }

  public CompletionStage<String> bookTrip(Trip trip){
      return componentClient.forEventSourcedEntity(trip.flightRef()).method(FlightBookingEntity::book)
              .invokeAsync()
              .thenApply(__ -> String.format("Flight %s booked", trip.flightRef()));
  }


  public static String extractJson(String response) {
    Pattern pattern = Pattern.compile("\\[.*?]", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(response);
    if (matcher.find()) {
      return matcher.group(0);
    }
    String message = "Could not extract json from response";
    log.error("{}: {}",message, response);
    throw new IllegalArgumentException(message);
  }
}
