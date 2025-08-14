package com.tripagent.application.agents.tools;

import akka.javasdk.JsonSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param id
 * @param name
 * @param neighborhood
 * @param availableFrom
 * @param availableUntil
 * @param pricePerNight in dollars
 */
public record AccommodationAPIResponse(
  String id,
  String name,
  String neighborhood,
  ZonedDateTime availableFrom,
  ZonedDateTime availableUntil,
  int pricePerNight
) {
  private static final Logger log = LoggerFactory.getLogger(AccommodationAPIResponse.class);

  public static List<AccommodationAPIResponse> extract(InputStream json) {
    try {
      return JsonSupport.getObjectMapper()
        .readValue(json, new TypeReference<List<AccommodationAPIResponse>>() {});
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
