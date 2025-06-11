package com.tripagent.application.agents;

import java.io.InputStream;
import java.util.List;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import com.tripagent.application.agents.tools.AccommodationAPIResponse;

@ComponentId("search-accommodation-agent")
public class AccommodationSearchAgent extends Agent {

  private static final String SYSTEM_MESSAGE =
      """
            You are a search accommodations agent in charge of looking for  accommodations,
            find ONLY accommodations within the following constraints %s. Ignore any constraints that don't refer accommodations
            If some error shows in the tool you are using do not provide any accommodations.

            Your response should follow a strict json schema like the following:
            { "accommodations":
               [
                         {
                           "id": 101,
                           "name": "Shinjuku Garden Inn",
                           "neighborhood": "Shinjuku",
                           "checkin": "2026-05-07T15:00:00Z",
                           "checkout": "2026-05-14T11:00:00Z",
                           "pricepernight": 120
                         },
                         {
                           "id": 102,
                           "name": "Asakusa Riverside Hotel",
                           "neighborhood": "Asakusa",
                           "checkin": "2026-05-07T15:00:00Z",
                           "checkout": "2026-05-14T11:00:00Z",
                           "pricepernight": 85
                         }
                         ...
            }

            Do not include any explanations or text outside of the JSON structure.
            Do include as many accommodations as possible.
            """
          .stripIndent();

  public Effect<AccommodationAPIResponseList> findAccommodations(String question) {
    return effects()
        .systemMessage(SYSTEM_MESSAGE)
        .userMessage(question)
        .responseAs(AccommodationAPIResponseList.class)
        .thenReply();
  }

  @FunctionTool(name = "find-accommodations", description = "finds accommodations")
  private List<AccommodationAPIResponse> findAccommodationsTool() {
    // If the accommodations results weren't fake you should add params
    InputStream in = getClass().getClassLoader().getResourceAsStream("accommodations.json");
    return AccommodationAPIResponse.extract(in);
  }

  public record AccommodationAPIResponseList(List<AccommodationAPIResponse> accommodations) {}
}
