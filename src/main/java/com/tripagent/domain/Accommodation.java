package com.tripagent.domain;

public record Accommodation(
    String id,
    String name,
    String neighborhood,
    String checkin,
    String checkout,
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
