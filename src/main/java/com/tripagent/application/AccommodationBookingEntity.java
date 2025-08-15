package com.tripagent.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import com.tripagent.domain.Accommodation;
import com.tripagent.domain.AccommodationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("accommodation-booking-specialist")
public class AccommodationBookingEntity
    extends EventSourcedEntity<Accommodation, AccommodationEvent> {

  private static final Logger log = LoggerFactory.getLogger(AccommodationBookingEntity.class);

  public Effect<Done> create(Accommodation accommodation) {
    if (currentState() == null) {
      log.info("Loading accommodation {} into the system.", accommodation);
      return effects()
          .persist(new AccommodationEvent.AccommodationFound(accommodation))
          .thenReply(__ -> Done.done());
    } else {
      log.warn("The accommodation {} is exists already.", accommodation);
      return effects().reply(Done.done());
    }
  }

  public Effect<Boolean> book() {
    if (currentState().status().equals(Accommodation.Status.AVAILABLE)) {
      log.info("Requesting to book {}.", currentState());
      return effects().persist(new AccommodationEvent.AccommodationSold()).thenReply(__ -> true);
    } else {
      log.warn(
          "The accommodation {} is not `available` anymore. Can't be requested", currentState());
      return effects().reply(false);
    }
  }

  public ReadOnlyEffect<Accommodation> getState() {
    if (currentState() == null) {
      return effects().error("no accommodation with that reference");
    } else {
      return effects().reply(currentState());
    }
  }

  @Override
  public Accommodation applyEvent(AccommodationEvent bookingAccommodationEvent) {
    return switch (bookingAccommodationEvent) {
      case AccommodationEvent.AccommodationFound ff ->
          ff.accommodation().withStatus(Accommodation.Status.AVAILABLE);
      case AccommodationEvent.AccommodationSold ignored ->
          currentState().withStatus(Accommodation.Status.BOOKED);
    };
  }
}
