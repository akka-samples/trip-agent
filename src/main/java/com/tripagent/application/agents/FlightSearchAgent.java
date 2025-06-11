package com.tripagent.application.agents;

import java.io.InputStream;
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
            that accommodate user requirements.
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
            Do include as many flights as possible.
            """
          .stripIndent();

  public Effect<FlightAPIResponseList> findFlights(String question) {
    return effects()
        .systemMessage(SYSTEM_MESSAGE)
        .userMessage(question)
        .responseAs(FlightAPIResponseList.class)
        .thenReply();
  }

  @FunctionTool(name = "find-flights", description = "finds flights")
  private List<FlightAPIResponse> findFlightsTool() {
    // If the flight results weren't fake you should add params
    InputStream in = getClass().getClassLoader().getResourceAsStream("flights.json");
    return FlightAPIResponse.extract(in);
  }

  public record FlightAPIResponseList(List<FlightAPIResponse> flights) {
    static FlightAPIResponseList empty() {
      return new FlightAPIResponseList(List.of());
    }
  }
}
