package com.tripagent.api;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import com.tripagent.application.AccommodationBookingEntity;
import com.tripagent.application.FlightBookingEntity;
import com.tripagent.application.TripAgentWorkflow;
import com.tripagent.domain.Accommodation;
import com.tripagent.domain.Flight;
import com.tripagent.domain.TripSearchState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/trip")
public class TripEndpoint {

  private final ComponentClient componentClient;
  private static final Logger log = LoggerFactory.getLogger(TripEndpoint.class);

  public TripEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  public record Question(String question) {}

  // check there's a email in content otherwise return the failures.
  @Post("/search/")
  public String searchTrip(Question question) {
    if (!TripSearchState.findEmail(question.question))
      return "Missing email to send you the results. Please add an email along the question";
    UUID uuid = UUID.randomUUID();
    return componentClient
        .forWorkflow(String.valueOf(uuid))
        .method(TripAgentWorkflow::startSearch)
        .invoke(question.question);
  }

  /**
   * @param flightRef
   * @param accommodationRef
   */
  public record BookingTripRequest(String flightRef, String accommodationRef) {}

  @Post("/book")
  public String book(BookingTripRequest bookingTripRequest) {
    componentClient
        .forEventSourcedEntity(bookingTripRequest.flightRef)
        .method(FlightBookingEntity::book)
        .invoke();
    componentClient
        .forEventSourcedEntity(bookingTripRequest.accommodationRef)
        .method(AccommodationBookingEntity::book)
        .invoke();
    return "Booking requested.";
  }

  private record FlightBookingResponse(
      String flightRef,
      String from,
      String to,
      ZonedDateTime departure,
      ZonedDateTime arrival,
      int price,
      Status status) {
    static FlightBookingResponse transform(Flight domainFlight) {
      Status status = Status.AVAILABLE;
      if (domainFlight.status().equals(Flight.Status.BOOKED)) {
        status = Status.BOOKED;
      }
      return new FlightBookingResponse(
          domainFlight.id(),
          domainFlight.from(),
          domainFlight.to(),
          domainFlight.departure(),
          domainFlight.arrival(),
          domainFlight.price(),
          status);
    }
  }

  enum Status {
    BOOKED,
    AVAILABLE
  }

  @Get("/flight/{id}")
  public FlightBookingResponse getFlight(String id) {
    Flight flight =
        componentClient.forEventSourcedEntity(id).method(FlightBookingEntity::getState).invoke();
    return FlightBookingResponse.transform(flight);
  }

  record AccommodationBookingResponse(
      String flightRef,
      String name,
      String neighborhood,
      ZonedDateTime checkin,
      ZonedDateTime checkout,
      int pricepernight,
      Status status) {
    static AccommodationBookingResponse transform(Accommodation domainAccommodation) {
      Status status = Status.AVAILABLE;
      if (domainAccommodation.status().equals(Accommodation.Status.BOOKED)) {
        status = Status.BOOKED;
      }
      return new AccommodationBookingResponse(
          domainAccommodation.id(),
          domainAccommodation.name(),
          domainAccommodation.neighborhood(),
          domainAccommodation.checkin(),
          domainAccommodation.checkout(),
          domainAccommodation.pricepernight(),
          status);
    }
  }

  @Get("/accommodation/{id}")
  public AccommodationBookingResponse getAccommodation(String id) {
    Accommodation accommodation =
        componentClient
            .forEventSourcedEntity(id)
            .method(AccommodationBookingEntity::getState)
            .invoke();
    return AccommodationBookingResponse.transform(accommodation);
  }

  @Get("/workflow/{uuid}")
  public String getWorkflow(String uuid) {
    Optional<TripSearchState> workflowState =
        componentClient.forWorkflow(uuid).method(TripAgentWorkflow::getState).invoke();
    if (workflowState.isEmpty()) throw HttpException.notFound();
    return workflowState.get().toString();
  }
}
