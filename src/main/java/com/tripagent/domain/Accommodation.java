package com.tripagent.domain;

import java.time.ZonedDateTime;

public record Accommodation(
  String id,
  String name,
  String neighborhood,
  ZonedDateTime checkin,
  ZonedDateTime checkout,
  int pricePerNight,
  Status status
) {
  public Accommodation withStatus(Status status) {
    return new Accommodation(
      id,
      name,
      neighborhood,
      checkin,
      checkout,
      pricePerNight,
      status
    );
  }

  public enum Status {
    AVAILABLE,
    BOOKED,
  }
}
