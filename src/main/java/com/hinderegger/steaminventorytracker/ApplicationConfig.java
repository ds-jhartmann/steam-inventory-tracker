package com.hinderegger.steaminventorytracker;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static com.hinderegger.steaminventorytracker.SteamInventoryTrackerApplication.BASEURL;

@Configuration
public class ApplicationConfig {

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
