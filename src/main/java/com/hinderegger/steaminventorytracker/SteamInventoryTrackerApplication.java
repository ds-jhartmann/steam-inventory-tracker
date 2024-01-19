package com.hinderegger.steaminventorytracker;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SteamInventoryTrackerApplication {
  public static final AtomicInteger COUNTER = new AtomicInteger(0);

  /**
   * The URL prefix for API URLs.
   */
  public static final String API_PATH = "/api/v1/items";

  public static void main(String[] args) {
    SpringApplication.run(SteamInventoryTrackerApplication.class, args);
  }
}
