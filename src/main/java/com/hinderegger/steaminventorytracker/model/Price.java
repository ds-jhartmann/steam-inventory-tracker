package com.hinderegger.steaminventorytracker.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Price {
  private final double price;
  private final double median;
  private final LocalDateTime timestamp;
}
