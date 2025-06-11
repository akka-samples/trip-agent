package com.tripagent.application;

import akka.javasdk.agent.Agent;
import akka.javasdk.agent.ModelProvider;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripagent.ai.tools.AccommodationAPIResponse;
import com.tripagent.ai.tools.AccommodationBookingAPITool;
import com.tripagent.ai.tools.EmailAPITool;
import com.tripagent.ai.tools.FlightAPIResponse;

import java.io.InputStream;
import java.util.List;

@ComponentId("flight-search-agent")
public class FlightSearchAgent extends Agent {

    private static final String SYSTEM_MESSAGE =
            """
            You are a flight search agent in charge of looking for flights
            that accommodate user requirements.
            For example, if you find two flights then produce a json of the type: 
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
            ]}
            But if you find more than two flights add as many flights as found, accordingly.
            Your response should follow a strict json schema as defined bellow.
            Do not include any explanations or text outside of the JSON structure.
            """.stripIndent();

    public Effect<FlightAPIResponseList> findFlights(String question) {
       return effects()
               .systemMessage(SYSTEM_MESSAGE)
               .userMessage(question)
               .responseAs(FlightAPIResponseList.class)
               .thenReply();

    }

    //TODO make tool private?
    @FunctionTool(name = "find-flights", description = "finds flights")
    public List<FlightAPIResponse> findFlightsTool() {
        // If the flight results weren't fake you should add params
        InputStream in = getClass().getClassLoader().getResourceAsStream("flights.json");
        return FlightAPIResponse.extract(in);
    }

    public record FlightAPIResponseList(List<FlightAPIResponse> flights){
        static FlightAPIResponseList empty() {
            return new FlightAPIResponseList(List.of());
        }
    }
}
