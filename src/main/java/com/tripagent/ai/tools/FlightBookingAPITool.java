package com.tripagent.ai.tools;

import java.io.InputStream;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallback;

public class FlightBookingAPITool {

  @Tool(description = "find flights")
  public List<FlightAPIResponse> findFlights() {
    // If the flight results weren't fake you should add params
    InputStream in = getClass().getClassLoader().getResourceAsStream("flights.json");
    return FlightAPIResponse.extract(in);
  }

  private static final FlightBookingAPITool toolInfo = new FlightBookingAPITool();

  // Necessary to deal with parsing `departure` and `arrival` ZonedDateTime fields in
  // `FlightAPIResponse`.
  // otherwise just @Tools would suffice.
  public static MethodToolCallback getMethodToolCallback(String methodName) {
    return MethodToolCallbackHelper.getMethodToolCallback(toolInfo, methodName);
  }
}
