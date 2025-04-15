package com.lb.domain;

public sealed interface FlightEvent {

  record FlightFound(Flight flight) implements FlightEvent {}

  record FlightSold() implements FlightEvent {}
}
