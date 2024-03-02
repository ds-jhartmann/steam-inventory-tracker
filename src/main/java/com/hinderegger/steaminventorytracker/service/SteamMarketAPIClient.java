package com.hinderegger.steaminventorytracker.service;

import static reactor.core.Exceptions.isRetryExhausted;

import com.hinderegger.steaminventorytracker.SteamInventoryTrackerApplication;
import com.hinderegger.steaminventorytracker.model.Item;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
public class SteamMarketAPIClient {

  private final WebClient client;

  private final RateLimiter rateLimiter;

  private final String path;

  public SteamMarketAPIClient(
      final WebClient client,
      final RateLimiter rateLimiter,
      final @Value("${steam.path}") String path) {
    this.client = client;
    this.rateLimiter = rateLimiter;
    this.path = path;
  }

  public Mono<String> getPriceForItem(final Item item) {
    String itemName = item.getItemName();

    log.info("Starting api request for item: " + itemName);

    try {
      return client
          .get()
          .uri(path + itemName)
          .retrieve()
          .bodyToMono(String.class)
          .doOnSubscribe(
              s ->
                  log.info(
                      "{} - {} - call triggered",
                      SteamInventoryTrackerApplication.COUNTER.incrementAndGet(),
                      LocalDateTime.now()))
          .transformDeferred(RateLimiterOperator.of(rateLimiter))
          .retryWhen(Retry.backoff(3, Duration.ofSeconds(1L)).filter(this::isError));
    } catch (IllegalStateException e) {
      if (isRetryExhausted(e)) {
        return Mono.error(e);
      } else {
        throw new IllegalStateException("Unexpected Exception occurred during Steam API request");
      }
    }
  }

  private boolean isError(Throwable throwable) {
    return ((WebClientResponseException) throwable).getStatusCode().isError();
  }
}
