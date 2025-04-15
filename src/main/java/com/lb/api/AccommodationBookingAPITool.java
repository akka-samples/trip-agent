package com.lb.api;

import akka.javasdk.client.ComponentClient;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;

public class AccommodationBookingAPITool {

  private final ComponentClient componentClient;

  public AccommodationBookingAPITool(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Tool
  public List<AccommodationAPIResponse> findAccommodation(
      ZonedDateTime fromDate,
      ZonedDateTime toDate,
      String city,
      String neighborhood,
      int maxTotalPrice) {
    InputStream in =
        AccommodationBookingAPITool.class
            .getClassLoader()
            .getResourceAsStream("accommodations.json");
    return AccommodationAPIResponse.extract(in);
  }
}
