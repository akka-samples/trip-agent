package com.tripagent.domain;

import akka.javasdk.annotations.TypeName;

public sealed interface AccommodationEvent {
  @TypeName("accommodation-found")
  record AccommodationFound(Accommodation accommodation) implements AccommodationEvent {}

  @TypeName("accommodation-sold")
  record AccommodationSold() implements AccommodationEvent {}
}
