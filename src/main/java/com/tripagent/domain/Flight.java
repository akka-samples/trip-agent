package com.tripagent.domain;

public record Flight(
    String id, String from, String to, String departure, String arrival, int price, Status status) {

  public Flight withStatus(Status status) {
    return new Flight(id, from, to, departure, arrival, price, status);
  }

  public enum Status {
    AVAILABLE,
    BOOKED
  }
}
