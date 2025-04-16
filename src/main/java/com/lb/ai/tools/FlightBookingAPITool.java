package com.lb.ai.tools;

import java.io.InputStream;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;

public class FlightBookingAPITool {

  @Tool
  public List<FlightAPIResponse> findFlights() {
    // If the flight results weren't fake we should add params
    InputStream in = getClass().getClassLoader().getResourceAsStream("flights.json");
    return FlightAPIResponse.extract(in);
  }
}
