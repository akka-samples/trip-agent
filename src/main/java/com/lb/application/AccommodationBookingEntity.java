package com.lb.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import com.lb.domain.Accommodation;
import com.lb.domain.AccommodationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("accommodation-booking-specialist")
public class AccommodationBookingEntity
    extends EventSourcedEntity<Accommodation, AccommodationEvent> {

  private static final Logger log = LoggerFactory.getLogger(AccommodationBookingEntity.class);

  @Override
  public Accommodation emptyState() {
    return Accommodation.empty();
  }

  public Effect<Done> create(Accommodation accommodation) {
    if (currentState().status().equals(Accommodation.Status.UNINITIALIZED)) {
      log.info("Loading accommodation {} into the system.", accommodation);
      return effects()
          .persist(new AccommodationEvent.AccommodationFound(accommodation))
          .thenReply(__ -> Done.done());
    } else {
      log.warn("The accommodation {} is exists already.", accommodation);
      return effects().reply(Done.done());
    }
  }

  /**
   * For simplicity, 1. we assume the imaginary API to book the accommodation always works and
   * reserves immediately the accommodation. Although it might be interesting to deal with a
   * previous state (requested) to show how to handle some of the complexity of the domain. 2. we
   * ignore the fact that some credential need to be used to pay the booking.
   *
   * @return Done
   */
  public Effect<Done> book() {
    if (currentState().status().equals(Accommodation.Status.AVAILABLE)) {
      log.info("Requesting to book {}.", currentState());
      return effects()
          .persist(new AccommodationEvent.AccommodationSold())
          .thenReply(__ -> Done.done());
    } else {
      log.warn(
          "The accommodation {} is not `available` anymore. Can't be requested", currentState());
      return effects().reply(Done.done());
    }
  }

  @Override
  public Accommodation applyEvent(AccommodationEvent bookingAccommodationEvent) {
    return switch (bookingAccommodationEvent) {
      case AccommodationEvent.AccommodationFound ff -> ff.accommodation();
      case AccommodationEvent.AccommodationSold ignored ->
          currentState().withStatus(Accommodation.Status.BOOKED);
    };
  }
}
