package com.lb.domain;

import com.lb.api.FlightAPIResponse;

// TODO use plain DateTime java
public record Flight(FlightAPIResponse flightAPIResponse, Status status) {

  public Flight(FlightAPIResponse flightAPIResponse) {
    this(flightAPIResponse, Status.OPEN);
  }

  public enum Status {
    UNINITIALIZED,
    OPEN,
    REQUESTED,
    BOOKED
  }
}
