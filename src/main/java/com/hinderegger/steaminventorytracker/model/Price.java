package com.hinderegger.steaminventorytracker.model;

import java.time.LocalDateTime;

public record Price(double price, double median, LocalDateTime timestamp) {}
