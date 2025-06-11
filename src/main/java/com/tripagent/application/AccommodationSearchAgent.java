package com.tripagent.application;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import com.tripagent.ai.tools.AccommodationAPIResponse;
import com.tripagent.ai.tools.AccommodationBookingAPITool;

import java.io.InputStream;
import java.util.List;

@ComponentId("search-accommodation-agent")
public class AccommodationSearchAgent extends Agent {

    private static final String SYSTEM_MESSAGE =
            """
            You are a search accommodations agent in charge of looking for  accommodations,
            find ONLY accommodations within the following constraints %s. Ignore any constraints that don't refer accommodations
            If some error shows in the tool you are using do not provide any accommodations.
            """.stripIndent();

    public Effect<AccommodationAPIResponseList> findAccommodations(String question) {
       return effects()
               .systemMessage(SYSTEM_MESSAGE)
               .userMessage(question)
               .responseAs(AccommodationAPIResponseList.class)
               .thenReply();

    }

    //TODO make tool private?
    @FunctionTool(name = "find-accommodations", description = "finds accommodations")
    public AccommodationAPIResponseList findAccommodationsTool() {
        // If the accommodations results weren't fake you should add params
        InputStream in =
                AccommodationBookingAPITool.class
                        .getClassLoader()
                        .getResourceAsStream("accommodations.json");
        return new AccommodationAPIResponseList(AccommodationAPIResponse.extract(in));
    }

   record AccommodationAPIResponseList(List<AccommodationAPIResponse> accommodations) {}


}
