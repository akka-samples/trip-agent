package com.tripagent.application.agents;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import com.tripagent.application.agents.tools.AccommodationAPIResponse;

@ComponentId("search-accommodation-agent")
public class AccommodationSearchAgent extends Agent {

  private static final String SYSTEM_MESSAGE =
      """
            You are a accommodations search agent in charge of looking for accommodations
            that are between the boundaries of user requirements.
            Your response should follow a json schema like the following:
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
            If no accommodations are found then your response must be:
            { "accommodations":[] }
            """
          .stripIndent();

  public Effect<AccommodationAPIResponseList> findAccommodations(String question) {
    return effects()
        .systemMessage(SYSTEM_MESSAGE)
        .userMessage(question)
        .responseAs(AccommodationAPIResponseList.class)
        .thenReply();
  }

  @FunctionTool(
      name = "find-accommodations",
      description =
          "Finds available accommodations for the given check-in and check-out dates. Format of the dates must be `2026-05-07T15:00:00Z`")
  private List<AccommodationAPIResponse> findAccommodationsTool(String checkin, String checkout) {
    ZonedDateTime checkinDate = ZonedDateTime.parse(checkin);
    ZonedDateTime checkoutDate = ZonedDateTime.parse(checkout);

    InputStream in = getClass().getClassLoader().getResourceAsStream("accommodations.json");
    List<AccommodationAPIResponse> found = AccommodationAPIResponse.extract(in);
    return found.stream()
        .filter(
            accmm ->
                (accmm.availableFrom().isEqual(checkinDate)
                        || accmm.availableFrom().isBefore(checkinDate))
                    && (accmm.availableUntil().isEqual(checkoutDate)
                        || accmm.availableUntil().isAfter(checkoutDate)))
        .map(
            each -> {
              return new AccommodationAPIResponse(
                  each.id(),
                  each.name(),
                  each.neighborhood(),
                  checkinDate,
                  checkoutDate,
                  each.pricePerNight());
            })
        .toList();
  }

  public record AccommodationAPIResponseList(List<AccommodationAPIResponse> accommodations) {}
}
