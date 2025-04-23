package com.lb.ai.tools;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallback;

public class AccommodationBookingAPITool {

  @Tool(description = "find accommodations")
  public List<AccommodationAPIResponse> findAccommodations() {
    // If the accommodations results weren't fake you should add params
    if (new Random().nextInt(5) % 5 == 0) throw new RuntimeException("Chaos monkey exception");
    InputStream in =
        AccommodationBookingAPITool.class
            .getClassLoader()
            .getResourceAsStream("accommodations.json");
    return AccommodationAPIResponse.extract(in);
  }

  private static final AccommodationBookingAPITool toolInfo = new AccommodationBookingAPITool();

  // Necessary to deal with parsing `checkin` and `checkout` ZonedDateTime fields in
  // `AccommodationAPIResponse`.
  // otherwise just @Tools would suffice.
  public static MethodToolCallback getMethodToolCallback(String methodName) {
    return MethodToolCallbackHelper.getMethodToolCallback(toolInfo, methodName);
  }
}
