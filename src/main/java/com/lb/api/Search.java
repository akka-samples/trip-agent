package com.lb.api;

public record Search(
    String id,
    String locationFrom,
    String locationTo,
    java.time.ZonedDateTime from,
    java.time.ZonedDateTime to,
    int flightMaxPrice,
    String neighborhood,
    int accMaxPrice,
    String email) {}
