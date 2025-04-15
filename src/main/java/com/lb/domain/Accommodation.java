package com.lb.domain;

import com.lb.api.AccommodationAPIResponse;

// TODO use plain DateTime java
public record Accommodation(AccommodationAPIResponse accommodationAPIResponse, Status status) {

    public Accommodation(AccommodationAPIResponse accommodationAPIResponse){
        this(accommodationAPIResponse, Status.AVAILABLE);
    }

    public enum Status {
        UNINITIALIZED,
        AVAILABLE,
        REQUESTED,
        BOOKED
    }
}