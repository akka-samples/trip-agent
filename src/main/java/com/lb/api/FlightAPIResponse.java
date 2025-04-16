package com.lb.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record FlightAPIResponse(
    String id, String from, String to, ZonedDateTime departure, ZonedDateTime arrival, int price) {

  private static final Logger log = LoggerFactory.getLogger(FlightAPIResponse.class);

  public static List<FlightAPIResponse> extract(InputStream json) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    try {
      return mapper.readValue(json, new TypeReference<List<FlightAPIResponse>>() {});
    } catch (IOException e) {
      log.error(e.toString());
      throw new RuntimeException(e);
    }
  }
}
