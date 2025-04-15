package com.lb.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record TripOptions(
    String description, List<Flight> flights, List<Accomodation> accommodations) {

  public static TripOptions empty() {
    return new TripOptions("empty trip", Collections.emptyList(), Collections.emptyList());
  }

  public TripOptions withDescription(String description) {
    return new TripOptions(description, flights, accommodations);
  }

  public TripOptions withFlight(Flight flight) {
    List<Flight> flights = new ArrayList<>(this.flights);
    flights.add(flight);
    return new TripOptions(description, Collections.unmodifiableList(flights), accommodations);
  }

  public TripOptions withFlights(List<Flight> flights) {
    List<Flight> finalFlights = new ArrayList<>(this.flights);
    finalFlights.addAll(flights);
    return new TripOptions(description, Collections.unmodifiableList(finalFlights), accommodations);
  }

  public TripOptions withAccommodation(Accomodation accomodation) {
    List<Accomodation> accommodations = new ArrayList<>(this.accommodations);
    accommodations.add(accomodation);
    return new TripOptions(description, flights, Collections.unmodifiableList(accommodations));
  }
}
