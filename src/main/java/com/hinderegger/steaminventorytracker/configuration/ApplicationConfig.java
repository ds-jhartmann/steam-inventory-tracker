package com.hinderegger.steaminventorytracker.configuration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.net.http.HttpClient;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@AllArgsConstructor
public class ApplicationConfig {

  private final SteamConfiguration config;

  @Bean
  public WebClient steamWebClient() {
    return WebClient.builder()
        .baseUrl(config.getBaseurl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Bean
  public RateLimiter steamRateLimiter() {
    return RateLimiter.of(
        "steam-rate-limiter",
        RateLimiterConfig.custom()
            .limitRefreshPeriod(config.getLimitRefreshPeriod())
            .limitForPeriod(config.getLimitForPeriod())
            .timeoutDuration(config.getTimeoutDuration())
            .build());
  }

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newHttpClient();
  }
}
