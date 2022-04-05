package com.hinderegger.steaminventorytracker.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Price {
    private final double price;
    private final double median;
    private final LocalDateTime timestamp;
}
