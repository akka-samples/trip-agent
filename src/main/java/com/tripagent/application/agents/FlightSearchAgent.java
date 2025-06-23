package com.tripagent.application.agents;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import com.tripagent.application.agents.tools.FlightAPIResponse;

@ComponentId("flight-search-agent")
public class FlightSearchAgent extends Agent {

  private static final String SYSTEM_MESSAGE =
      """
            You are a flight search agent in charge of looking for flights
            that are between the boundaries of user requirements.
            Your response should follow a json schema like the following:
            { "flights: [
            {
                      "id": "ABC123",
                      "from": "JFK",
                      "to": "LAX",
                      "departure": "2025-06-10T08:00:00Z",
                      "arrival": "2025-06-10T11:00:00Z",
                      "price": 350
                    },
                    {
                      "id": "ABC124",
                      "from": "JFK1",
                      "to": "LAX1",
                      "departure": "2025-06-10T08:00:00Z",
                      "arrival": "2025-06-10T11:00:00Z",
                      "price": 351
                    }
                    ...
            ]}
            Do not include any explanations or text outside of the JSON structure.
            If no flights are found then your response must be:
            { "flights":[] }
            """
          .stripIndent();

  public Effect<FlightAPIResponseList> findFlights(String question) {
    return effects()
        .systemMessage(SYSTEM_MESSAGE)
        .userMessage(question)
        .responseAs(FlightAPIResponseList.class)
        .thenReply();
  }

  @FunctionTool(
      name = "find-flights",
      description =
          "Finds available flights for the given outbound and return dates. Format of the dates must be `2026-05-07T15:00:00Z`")
  private List<FlightAPIResponse> findFlightsTool(String outbound, String returnDate) {
    ZonedDateTime outboundParam = ZonedDateTime.parse(outbound);
    ZonedDateTime returnLegParam = ZonedDateTime.parse(returnDate);

    InputStream in = getClass().getClassLoader().getResourceAsStream("flights.json");
    List<FlightAPIResponse> found = FlightAPIResponse.extract(in);
    return found.stream()
        .filter(
            flight -> {
              return flight.departure().toLocalDate().equals(outboundParam.toLocalDate())
                  || flight.returnLeg().toLocalDate().equals(returnLegParam.toLocalDate());
            })
        .toList();
  }

  public record FlightAPIResponseList(List<FlightAPIResponse> flights) {}
}
