package com.lb.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import com.lb.domain.Flight;
import com.lb.domain.FlightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("flight-booking-specialist")
public class FlightBookingEntity extends EventSourcedEntity<Flight, FlightEvent> {

  private static final Logger log = LoggerFactory.getLogger(FlightBookingEntity.class);

  public Effect<Done> create(Flight flight) {
    if (currentState() == null) {
      log.info("Loading flight {} into the system.", flight);
      return effects().persist(new FlightEvent.FlightFound(flight)).thenReply(__ -> Done.done());
    } else {
      log.warn("The flight {} is exists already.", flight);
      return effects().reply(Done.done());
    }
  }

  public Effect<Boolean> book() {
    if (currentState().status().equals(Flight.Status.AVAILABLE)) {
      log.info("Requesting to book {}.", currentState());
      return effects().persist(new FlightEvent.FlightSold()).thenReply(__ -> true);
    } else {
      log.warn("The flight {} is not `open` anymore. Can't be requested", currentState());
      return effects().reply(false);
    }
  }

  public ReadOnlyEffect<Flight> getState() {
    if (currentState() == null) {
      return effects().error("no flight with that reference");
    } else {
      return effects().reply(currentState());
    }
  }

  @Override
  public Flight applyEvent(FlightEvent bookingFlightEvent) {
    return switch (bookingFlightEvent) {
      case FlightEvent.FlightFound ff -> ff.flight().withStatus(Flight.Status.AVAILABLE);
      case FlightEvent.FlightSold ignored -> currentState().withStatus(Flight.Status.BOOKED);
    };
  }
}
