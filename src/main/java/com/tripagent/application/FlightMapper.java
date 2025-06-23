package com.tripagent.application;

import java.util.List;

import com.tripagent.application.agents.tools.FlightAPIResponse;
import com.tripagent.domain.Flight;

public class FlightMapper {

  public static List<Flight> mapFlights(List<FlightAPIResponse> flights) {
    return flights.stream().map(FlightMapper::mapFlight).toList();
  }

  public static Flight mapFlight(FlightAPIResponse f) {
    return new Flight(
        f.id(), f.from(), f.to(), f.departure(), f.returnLeg(), f.price(), Flight.Status.AVAILABLE);
  }
}
