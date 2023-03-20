package com.hinderegger.steaminventorytracker.configuration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class ApplicationConfig {

    @Value("${steam.baseurl}")
    private String baseUrl;

    @Bean(name = "steamWebClient")
    public WebClient steamWebClient() {
        return WebClient.builder().baseUrl(baseUrl).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
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
