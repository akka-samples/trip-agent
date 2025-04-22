package com.lb.application;

import akka.javasdk.client.ComponentClient;
import com.lb.api.*;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripCoordinator {

  private static final Logger log = LoggerFactory.getLogger(TripCoordinator.class);

  private final ComponentClient componentClient;

  public TripCoordinator(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  //  public String requestTrip(String question) {
  //    // TODO? use virtual threads
  //
  // componentClient.forWorkflow("trip-agent").method(TripAgentWorkflow::startSearch).invokeAsync(question);
  //    // TODO? search for already existing flights before using the "external" API.
  //  ;
  //
  //
  //    String responseAccommodations =
  //        chatClient
  //            .prompt(
  //                String.format(
  //                    """
  //                        find ONLY accommodations with the following constraints %s. Ignore any
  // constraints that don't refer accommodations
  //                        Parse the accommodations as a JSON such they fit a schema parseable to a
  // Java class like this:
  //                        AccommodationAPIResponse( String id, String name, String neighborhood,
  // ZonedDateTime checkin, ZonedDateTime checkout, int pricepernight)
  //                        Create the JSON such it has only a list of AccommodationAPIResponse, do
  // not add any other field
  //                        """,
  //                    question))
  //            .tools(AccommodationBookingAPITool.getMethodToolCallback("findAccommodations"))
  //            .call()
  //            .content();
  //
  //    storeAccommodations(responseAccommodations);
  //    log.info("sending mail");
  //    String responseMail =
  //        chatClient
  //            .prompt(
  //                String.format(
  //                    """
  //                        You are allowed to use the @tool function only once in this
  // conversation. Do not use it more than once, even if more information becomes available
  //                        Send an email to the email provided in %s, using the requestId provided
  // in %s and the content from %s and %s. The content has flights and accommodations
  //                        Add in the email a recommendation with the best value combination flight
  // (outbound and return) and accommodation
  //                        parse the whole content as HTML before sending
  //                        """,
  //                    question, question, responseFlights, responseAccommodations))
  //            .tools(new EmailAPITool())
  //            .call()
  //            .content();
  //    log.debug(String.format("responseMail %s", responseMail));
  //    return "We are processing your userRequest. We'll send you the response to your email in a
  // minute.";
  //  }

//  public CompletionStage<String> bookTrip(TripEndpoint.BookingTripRequest bookingTripRequest) {
//    return componentClient
//        .forEventSourcedEntity(bookingTripRequest.flightRef())
//        .method(FlightBookingEntity::book)
//        .invokeAsync()
//        .thenApply(__ -> String.format("Flight %s booked", bookingTripRequest.flightRef()));
//  }



