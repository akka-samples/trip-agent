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

  @Override
  public Flight emptyState() {
    return Flight.empty();
  }

  public Effect<Done> create(Flight flight) {
    if (currentState().status().equals(Flight.Status.UNINITIALIZED)) {
      log.info("Loading flight {} into the system.", flight);
      return effects().persist(new FlightEvent.FlightFound(flight)).thenReply(__ -> Done.done());
    } else {
      log.warn("The flight {} is exists already.", flight);
      return effects().reply(Done.done());
    }
  }

  /**
   * For simplicity, 1. we assume the imaginary API to book the flight always works and reserves
   * immediately the flight. Although it might be interesting to deal with a previous state
   * (requested) to show how to handle some of the complexity of the domain. 2. we ignore the fact
   * that some credential need to be used to pay the booking.
   *
   * @return Done
   */
  public Effect<Done> book() {
    if (currentState().status().equals(Flight.Status.OPEN)) {
      log.info("Requesting to book {}.", currentState());
      return effects().persist(new FlightEvent.FlightSold()).thenReply(__ -> Done.done());
    } else {
      log.warn("The flight {} is not `open` anymore. Can't be requested", currentState());
      return effects().reply(Done.done());
    }
  }

  @Override
  public Flight applyEvent(FlightEvent bookingFlightEvent) {
    return switch (bookingFlightEvent) {
      case FlightEvent.FlightFound ff -> ff.flight();
      case FlightEvent.FlightSold ignored -> currentState().withStatus(Flight.Status.BOOKED);
    };
  }
}
