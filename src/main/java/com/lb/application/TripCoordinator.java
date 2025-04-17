package com.lb.application;

import akka.javasdk.client.ComponentClient;
import com.lb.ai.models.TripAgentChatModel;
import com.lb.ai.tools.*;
import com.lb.api.*;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

public class TripCoordinator {

  private static final Logger log = LoggerFactory.getLogger(TripCoordinator.class);

  private final ComponentClient componentClient;
  private final ChatClient chatClient;

  public TripCoordinator(TripAgentChatModel tripAgentChatModel, ComponentClient componentClient) {
    this.componentClient = componentClient;
    this.chatClient = ChatClient.create(tripAgentChatModel);
  }

  public String requestTrip(String question) {
    // TODO? use virtual threads
    // TODO? search for already existing flights before using the "external" API.
    log.info("looking for flights");
    String responseFlights =
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
            .call()
            .content();

    storeFlights(responseFlights);
    log.info("looking for accommodations ");

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

    storeAccommodations(responseAccommodations);
    log.info("sending mail");
    String responseMail =
        chatClient
            .prompt(
                String.format(
                    """
                        You are allowed to use the @tool function only once in this conversation. Do not use it more than once, even if more information becomes available
                        Send an email to the email provided in %s, using the requestId provided in %s and the content from %s and %s. The content has flights and accommodations
                        Add in the email a recommendation with the best value combination flight (outbound and return) and accommodation
                        parse the whole content as HTML before sending
                        """,
                    question, question, responseFlights, responseAccommodations))
            .tools(new EmailAPITool())
            .call()
            .content();
    log.debug(String.format("responseMail %s", responseMail));
    return "We are processing your request. We'll send you the response to your email in a minute.";
  }

  public CompletionStage<String> bookTrip(TripEndpoint.BookingTripRequest bookingTripRequest) {
    return componentClient
        .forEventSourcedEntity(bookingTripRequest.flightRef())
        .method(FlightBookingEntity::book)
        .invokeAsync()
        .thenApply(__ -> String.format("Flight %s booked", bookingTripRequest.flightRef()));
  }

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

  private void storeAccommodations(String chatResponseAccomodations) {
    log.debug("parsing accommodations: {}", chatResponseAccomodations);
    String onlyAccommodations = extractJson(chatResponseAccomodations);
    List<AccommodationAPIResponse> accommodationAPIResponses =
        AccommodationAPIResponse.extract(new ByteArrayInputStream(onlyAccommodations.getBytes()));
    log.debug("storing accommodations: {}", accommodationAPIResponses);
    // load accommodations into entities
    accommodationAPIResponses.forEach(
        accommodation -> {
          componentClient
              .forEventSourcedEntity(accommodation.id())
              .method(AccommodationBookingEntity::create)
              .invokeAsync(accommodation);
        });
  }

  private void storeFlights(String chatResponseFlights) {
    log.debug("parsing flights: {}", chatResponseFlights);
    String onlyFlights = extractJson(chatResponseFlights);
    List<FlightAPIResponse> flightAPIResponses =
        FlightAPIResponse.extract(new ByteArrayInputStream(onlyFlights.getBytes()));
    log.debug("storing flights: {}", flightAPIResponses);
    // load flights into entities
    flightAPIResponses.forEach(
        flight -> {
          componentClient
              .forEventSourcedEntity(flight.id())
              .method(FlightBookingEntity::create)
              .invokeAsync(flight);
        });
  }
}
