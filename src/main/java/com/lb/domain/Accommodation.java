package com.lb.domain;

import java.time.ZonedDateTime;

public record Accommodation(
    String id,
    String name,
    String neighborhood,
    ZonedDateTime checkin,
    ZonedDateTime checkout,
    int pricepernight,
    Status status) {

  public Accommodation withStatus(Status status) {
    return new Accommodation(id, name, neighborhood, checkin, checkout, pricepernight, status);
  }

  public enum Status {
    AVAILABLE,
    BOOKED
  }
}
