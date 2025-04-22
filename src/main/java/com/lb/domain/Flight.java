package com.lb.domain;

import java.time.ZonedDateTime;

// TODO use plain DateTime java
public record Flight(
    String id,
    String from,
    String to,
    ZonedDateTime departure,
    ZonedDateTime arrival,
    int price,
    Status status) {

  //  public Flight(FlightAPIResponse flightAPIResponse) {
  //    this(flightAPIResponse, Status.OPEN);
  //  }

  public static Flight empty() {
    return new Flight(null, null, null, null, null, 0, Status.UNINITIALIZED);
  }

  public Flight withStatus(Status status) {
    return new Flight(id, from, to, departure, arrival, price, status);
  }

  public enum Status {
    UNINITIALIZED,
    OPEN,
    REQUESTED,
    BOOKED
  }
}
