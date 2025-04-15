package com.lb.domain;

public sealed interface AccommodationEvent {

  record AccommodationFound(Accommodation accommodation) implements AccommodationEvent {}

  record AccommodationSold() implements AccommodationEvent {}
}
