package com.lb.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

public record AccommodationAPIResponse(
    String id,
    String name,
    String neighborhood,
    ZonedDateTime checkin,
    ZonedDateTime checkout,
    int pricepernight) {

  private static final Logger log = LoggerFactory.getLogger(AccommodationAPIResponse.class);

  public static List<AccommodationAPIResponse> extract(InputStream json) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    try {
      return mapper.readValue(json, new TypeReference<List<AccommodationAPIResponse>>() {});
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
