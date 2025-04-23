package com.lb.ai.tools;

import java.io.InputStream;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallback;

public class AccommodationBookingAPITool {

  @Tool(description = "find accommodations")
  public List<AccommodationAPIResponse> findAccommodations() {
    // If the accommodations results weren't fake you should add params
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
