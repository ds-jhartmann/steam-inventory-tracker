package com.hinderegger.steaminventorytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableScheduling
public class SteamInventoryTrackerApplication {
    public static final String PATH = "appid=730&currency=3&market_hash_name=";
    public static final AtomicInteger COUNTER = new AtomicInteger(0);
    public static final String BASEURL = "https://steamcommunity.com/market/priceoverview/?";

    public static void main(String[] args) {
        SpringApplication.run(SteamInventoryTrackerApplication.class, args);
    }
}
