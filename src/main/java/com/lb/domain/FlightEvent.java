package com.lb.domain;

import akka.javasdk.annotations.TypeName;

public sealed interface FlightEvent {

  @TypeName("flight-found")
  record FlightFound(Flight flight) implements FlightEvent {}

  @TypeName("flight-sold")
  record FlightSold() implements FlightEvent {}
}
