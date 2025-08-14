package com.tripagent.domain;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TripSearchState(String userRequest, Trip trip, RequestStatus requestState) {
  public TripSearchState withFlights(List<Flight> flights) {
    return new TripSearchState(
      userRequest,
      new Trip(flights, this.trip.accommodations),
      this.requestState
    );
  }

  public TripSearchState withAccommodations(List<Accommodation> accommodations) {
    return new TripSearchState(
      userRequest,
      new Trip(this.trip.flights, accommodations),
      this.requestState
    );
  }

  public TripSearchState withRequestStatus(RequestStatus requestStatus) {
    return new TripSearchState(userRequest, this.trip, requestStatus);
  }

  public record Trip(List<Flight> flights, List<Accommodation> accommodations) {
    public static Trip empty() {
      return new Trip(List.of(), List.of());
    }
  }

  public record RequestStatus(StatusTag tag, Optional<String> description) {
    public RequestStatus(StatusTag tag) {
      this(tag, Optional.empty());
    }
  }
  public enum StatusTag {
    STARTED,
    SUCCESSFULLY_FINISHED,
    FAILED,
  }

  public static boolean findEmail(String request) {
    final Pattern EMAIL_PATTERN = Pattern.compile(
      "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    Matcher matcher = EMAIL_PATTERN.matcher(request);
    return matcher.find();
  }


}
