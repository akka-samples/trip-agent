package com.lb.domain;

import java.util.Date;

public record FlightConstraints(
    String from, String to, Date departure, Date arrival, int maxPrice) {}
