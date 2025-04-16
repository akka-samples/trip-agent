package com.lb.api;

import akka.javasdk.client.ComponentClient;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;

public class AccommodationBookingAPITool {


  public AccommodationBookingAPITool() {
  }

  @Tool
  public List<AccommodationAPIResponse> findAccommodations(){
  //If the accommodations results weren't fake we should add params
  InputStream in =
        AccommodationBookingAPITool.class
            .getClassLoader()
            .getResourceAsStream("accommodations.json");
    return AccommodationAPIResponse.extract(in);
  }
}
