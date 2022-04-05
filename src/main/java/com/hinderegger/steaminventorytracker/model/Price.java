package com.hinderegger.steaminventorytracker.model;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Price {
    private final double price;
    private final double median;
    private final ZonedDateTime timestamp;
}
