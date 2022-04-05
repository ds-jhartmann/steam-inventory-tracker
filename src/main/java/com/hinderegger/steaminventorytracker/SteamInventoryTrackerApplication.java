package com.hinderegger.steaminventorytracker;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class SteamInventoryTrackerApplication {
    public static final String PATH = "appid=730&currency=3&market_hash_name=";
    public static final AtomicInteger COUNTER = new AtomicInteger(0);
    public static final String BASEURL = "https://steamcommunity.com/market/priceoverview/?";

    public static void main(String[] args) {
        SpringApplication.run(SteamInventoryTrackerApplication.class, args);
    }

    @Bean(name = "steamWebClient")
    public WebClient steamWebClient() {
        return WebClient.builder().baseUrl(BASEURL).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }

    @Bean(name = "steamRateLimiter")
    public RateLimiter steamRateLimiter() {
        return RateLimiter.of("steam-rate-limiter",
                RateLimiterConfig
                        .custom()
                        .limitRefreshPeriod(Duration.ofSeconds(30L))
                        .limitForPeriod(1)
                        .timeoutDuration(Duration.ofMinutes(10L))
                        .build());
    }

}
