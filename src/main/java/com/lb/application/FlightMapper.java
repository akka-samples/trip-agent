package com.lb.application;

import com.lb.ai.tools.FlightAPIResponse;
import com.lb.domain.Flight;
import java.util.List;

public class FlightMapper {

  // TODO probably not needed
  public static List<Flight> mapFlights(List<FlightAPIResponse> flights) {
    return flights.stream()
        .map(
            f -> {
              return new Flight(
                  f.id(),
                  f.from(),
                  f.to(),
                  f.departure(),
                  f.arrival(),
                  f.price(),
                  Flight.Status.UNINITIALIZED);
            })
        .toList();
  }

  public static Flight mapFlight(FlightAPIResponse f) {
    return new Flight(
        f.id(),
        f.from(),
        f.to(),
        f.departure(),
        f.arrival(),
        f.price(),
        Flight.Status.UNINITIALIZED);
  }
}
