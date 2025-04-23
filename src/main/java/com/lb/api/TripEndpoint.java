package com.lb.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.lb.ai.models.TripAgentChatModel;
import com.lb.application.TripAgentWorkflow;
import com.lb.domain.TripSearchState;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/trip")
public class TripEndpoint {

  private final ComponentClient componentClient;
  private static final Logger log = LoggerFactory.getLogger(TripEndpoint.class);

  public TripEndpoint(ComponentClient componentClient, TripAgentChatModel tripAgentChatModel) {
    this.componentClient = componentClient;
  }

  // check there's a email in content otherwise return the failures.
  @Post("/search/")
  public String searchTrip(Question question) {
    if (!TripSearchState.findEmail(question.question))
      return "Missing email to send you the results. Please add an email along the question";
    componentClient
        .forWorkflow(String.valueOf(question.hashCode()))
        .method(TripAgentWorkflow::startSearch)
        .invoke(question.question);
    return "Request received. We'll sent an email shortly.";
  }

  @Post("/book")
  public CompletionStage<String> book(BookingTripRequest bookingTripRequest) {
    throw new NotImplementedException("Not implemented. Out of scope");
  }

  /**
   * @param flightRef
   * @param accommodationRef
   */
  public record BookingTripRequest(String flightRef, String accommodationRef) {}

  public record Question(String question) {}
}
