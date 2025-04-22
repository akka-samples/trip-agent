package com.lb.domain;

import java.util.List;
import java.util.stream.Stream;

public record TripSearchState(Trip trip, RequestStatus requestState) {

  public TripSearchState withFlights(List<Flight> flights) {
    return new TripSearchState(new Trip(flights, this.trip.accommodations), this.requestState);
  }

  public TripSearchState withAccommodations(List<Accommodation> accommodations) {
    return new TripSearchState(new Trip(this.trip.flights, accommodations), this.requestState);
  }

  public TripSearchState withFlights(Stream<Flight> flights) {
    return withFlights(flights.toList());
  }

  public record Trip(List<Flight> flights, List<Accommodation> accommodations) {
    public static Trip empty() {
      return new Trip(List.of(), List.of());
    }
  }

  public enum RequestStatus {
    RECEIVED,
    PROCESSING,
    PROCESSED,
    FAILED
  }
}
