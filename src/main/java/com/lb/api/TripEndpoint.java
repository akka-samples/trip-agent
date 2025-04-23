package com.lb.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import com.lb.ai.models.TripAgentChatModel;
import com.lb.application.TripAgentWorkflow;
import com.lb.domain.TripSearchState;
import java.util.Optional;
import java.util.UUID;
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

  public record Question(String question) {}
  // check there's a email in content otherwise return the failures.
  @Post("/search/")
  public String searchTrip(Question question) {
    if (!TripSearchState.findEmail(question.question))
      return "Missing email to send you the results. Please add an email along the question";
    UUID uuid = UUID.randomUUID();
    return componentClient
        .forWorkflow(String.valueOf(uuid))
        .method(TripAgentWorkflow::startSearch)
        .invoke(question.question);
  }

  /**
   * @param flightRef
   * @param accommodationRef
   */
  public record BookingTripRequest(String flightRef, String accommodationRef) {}
  @Post("/book")
  public String book(BookingTripRequest bookingTripRequest) {
    throw new NotImplementedException("Not implemented. Out of scope");
  }

  @Get("/{uuid}")
  public String checkWorkflow(String uuid) {
    Optional<TripSearchState> workflowState =
        componentClient.forWorkflow(uuid).method(TripAgentWorkflow::getState).invoke();
    if (workflowState.isEmpty()) throw HttpException.notFound();
    return workflowState.get().toString();
  }



}
