package com.hinderegger.steaminventorytracker.service;

import static reactor.core.Exceptions.isRetryExhausted;

import com.hinderegger.steaminventorytracker.SteamInventoryTrackerApplication;
import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
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
  private final TimeProvider timeProvider;
  private final SteamConfiguration steamConfiguration;

  public SteamMarketAPIClient(
      final WebClient client,
      final RateLimiter rateLimiter,
      final @Value("${steam.path}") String path,
      final TimeProvider timeProvider,
      final SteamConfiguration steamConfiguration) {
    this.client = client;
    this.rateLimiter = rateLimiter;
    this.path = path;
    this.timeProvider = timeProvider;
    this.steamConfiguration = steamConfiguration;
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
                      timeProvider.now()))
          .transformDeferred(RateLimiterOperator.of(rateLimiter))
          .retryWhen(createRetrySpec());
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

  /**
   * Creates a retry specification using the configured retry parameters.
   *
   * @return the retry specification
   */
  private Retry createRetrySpec() {
    return Retry.backoff(
            steamConfiguration.getMaxRetryAttempts(), steamConfiguration.getRetryInitialBackoff())
        .maxBackoff(steamConfiguration.getRetryMaxBackoff())
        .jitter(steamConfiguration.getRetryJitter())
        .filter(this::isError);
  }
}
