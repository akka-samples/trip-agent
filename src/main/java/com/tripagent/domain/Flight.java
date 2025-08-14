package com.tripagent.domain;

import java.time.ZonedDateTime;

public record Flight(
  String id,
  String from,
  String to,
  ZonedDateTime departure,
  ZonedDateTime arrival,
  int price,
  Status status
) {
  public Flight withStatus(Status status) {
    return new Flight(id, from, to, departure, arrival, price, status);
  }

  public enum Status {
    AVAILABLE,
    BOOKED,
  }
}
