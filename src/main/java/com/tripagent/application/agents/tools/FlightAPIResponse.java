package com.tripagent.application.agents.tools;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

import akka.javasdk.JsonSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record FlightAPIResponse(
    String id,
    String from,
    String to,
    ZonedDateTime departure,
    ZonedDateTime returnLeg,
    int price) {

  private static final Logger log = LoggerFactory.getLogger(FlightAPIResponse.class);

  public static List<FlightAPIResponse> extract(InputStream json) {
    try {
      return JsonSupport.getObjectMapper()
          .readValue(json, new TypeReference<List<FlightAPIResponse>>() {});
    } catch (IOException e) {
      log.error(e.toString());
      throw new RuntimeException(e);
    }
  }
}
