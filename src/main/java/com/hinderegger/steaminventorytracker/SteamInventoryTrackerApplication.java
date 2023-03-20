package com.hinderegger.steaminventorytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableScheduling
public class SteamInventoryTrackerApplication {
    public static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static void main(String[] args) {
        SpringApplication.run(SteamInventoryTrackerApplication.class, args);
    }
}
