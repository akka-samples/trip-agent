package com.lb.api;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;

public class FlightBookingAPITool {

  @Tool
  public List<FlightAPIResponse> findFlights(
      ZonedDateTime fromDate,
      ZonedDateTime toDate,
      String fromWhere,
      String toWhere) {
    //If the flight results weren't fake this params would be used.
    InputStream in = getClass().getClassLoader().getResourceAsStream("flights.json");
    return FlightAPIResponse.extract(in);
  }
}
