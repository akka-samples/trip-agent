package com.lb.application;

import com.lb.ai.tools.AccommodationAPIResponse;
import com.lb.domain.Accommodation;
import java.util.List;

public class AccommodationMapper {

  // TODO probably not needed
  public static List<Accommodation> mapAccommodations(
      List<AccommodationAPIResponse> accommodations) {
    return accommodations.stream().map(AccommodationMapper::mapAccommodation).toList();
  }

  public static Accommodation mapAccommodation(AccommodationAPIResponse a) {
    return new Accommodation(
        a.id(),
        a.name(),
        a.neighborhood(),
        a.checkin(),
        a.checkout(),
        a.pricepernight(),
        Accommodation.Status.AVAILABLE);
  }
}
