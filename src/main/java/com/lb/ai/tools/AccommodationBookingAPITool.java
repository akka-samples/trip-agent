package com.lb.ai.tools;

import java.io.InputStream;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;

public class AccommodationBookingAPITool {

  @Tool
  public List<AccommodationAPIResponse> findAccommodations() {
    // If the accommodations results weren't fake we should add params
    InputStream in =
        AccommodationBookingAPITool.class
            .getClassLoader()
            .getResourceAsStream("accommodations.json");
    return AccommodationAPIResponse.extract(in);
  }
}
