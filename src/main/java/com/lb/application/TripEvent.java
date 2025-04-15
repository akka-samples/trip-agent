package com.lb.application;

import akka.javasdk.annotations.TypeName;

public sealed interface TripEvent {

  @TypeName("trip-requested")
  record TripRequested() implements TripEvent {}

  @TypeName("trip-accepted")
  record TripAccepted() implements TripEvent {}

  // More type names...
  record TripFound() implements TripEvent {}

  record FlightFound() implements TripEvent {}

  record FlightNotFound() implements TripEvent {}

  record AccommodationFound() implements TripEvent {}

  record AccommodationNotFound() implements TripEvent {}
}
