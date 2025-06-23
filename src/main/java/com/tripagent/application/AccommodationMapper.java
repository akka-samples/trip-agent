package com.tripagent.application;

import java.util.List;

import com.tripagent.application.agents.tools.AccommodationAPIResponse;
import com.tripagent.domain.Accommodation;

public class AccommodationMapper {

  public static List<Accommodation> mapAccommodations(
      List<AccommodationAPIResponse> accommodations) {
    return accommodations.stream().map(AccommodationMapper::mapAccommodation).toList();
  }

  public static Accommodation mapAccommodation(AccommodationAPIResponse a) {
    return new Accommodation(
        a.id(),
        a.name(),
        a.neighborhood(),
        a.availableFrom(),
        a.availableUntil(),
        a.pricePerNight(),
        Accommodation.Status.AVAILABLE);
  }
}
