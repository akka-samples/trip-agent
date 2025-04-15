package com.lb.domain;

import java.util.Date;

public record TripConstraints(String from, String to, Date when) {

  public String description() {
    return toString();
  }

  @Override
  public String toString() {
    return "TripConstraints{"
        + "from='"
        + from
        + '\''
        + ", to='"
        + to
        + '\''
        + ", when="
        + when
        + '}';
  }
}
