package com.lb.api;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;

public class FlightBookingAPITool {

  //    @Tool
  //    public findFlights(FlightConstraints constraints){
  //        return componentClient
  //                .forEventSourcedEntity(String.valueOf(constraints.hashCode()))
  //                .method(FlightBookingSpecialist::)
  //                        .
  //
  //    }

  @Tool // TODO this params are not used
  public List<FlightAPIResponse> findFlights(
      ZonedDateTime fromDate,
      ZonedDateTime toDate,
      String fromWhere,
      String toWhere,
      int maxPrice) {
    InputStream in = getClass().getClassLoader().getResourceAsStream("flights.json");
    return FlightAPIResponse.extract(in);
  }
}
