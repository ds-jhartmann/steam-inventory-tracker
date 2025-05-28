package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SteamMarketAPIClientTest {

  private SteamMarketAPIClient testee;
  @Mock private ExchangeFunction exchangeFunction;

  @BeforeEach
  void setUp() {
    final WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();

    // Create a real RateLimiter but with very small time values (milliseconds instead of seconds)
    final RateLimiter rateLimiter =
        RateLimiter.of(
            "test",
            RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(1)) // 1 millisecond instead of seconds
                .limitForPeriod(1)
                .timeoutDuration(Duration.ofMillis(5)) // 5 milliseconds instead of seconds
                .build());

    // Create a SteamConfiguration with test retry parameters
    final SteamConfiguration steamConfiguration = new SteamConfiguration();
    steamConfiguration.setMaxRetryAttempts(3);
    steamConfiguration.setRetryInitialBackoff(Duration.ofMillis(1));
    steamConfiguration.setRetryMaxBackoff(Duration.ofMillis(10));
    steamConfiguration.setRetryJitter(0.5);

    final MockTimeProvider mockTimeProvider = new MockTimeProvider();
    testee =
        new SteamMarketAPIClient(webClient, rateLimiter, "?market_hash_name=", mockTimeProvider, steamConfiguration);
  }

  @Test
  void shouldReturnPriceOnSuccess() {
    // Arrange
    final ClientResponse clientResponse =
        ClientResponse.create(HttpStatusCode.valueOf(200))
            .body(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}")
            .build();
    when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(clientResponse));

    // Act
    final String result = testee.getPriceForItem(new Item("Test Item", List.of())).block();

    // Assert
    assertThat(result)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");
    verify(exchangeFunction, times(1)).exchange(any());
  }

  @Test
  void shouldWaitForLimiterAmountInSubsequentRequests() {
    // Arrange
    final ClientResponse clientResponse =
        ClientResponse.create(HttpStatusCode.valueOf(200))
            .body(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}")
            .build();
    when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(clientResponse));

    final StopWatch test = new StopWatch("test");

    // Act
    test.start();
    final String result1 = testee.getPriceForItem(new Item("Test Item", List.of())).block();
    final String result2 = testee.getPriceForItem(new Item("Test Item 2", List.of())).block();
    test.stop();

    // Assert
    // The test should complete very quickly since we're using millisecond values for the RateLimiter
    assertThat(test.getTotalTimeMillis()).isLessThan(100); // Should complete in less than 100ms

    assertThat(result1)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");
    assertThat(result2)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");

    verify(exchangeFunction, times(2)).exchange(any());
  }

  @Test
  void shouldRetryOn4xxError() {
    // Arrange
    final ClientResponse clientResponse =
        ClientResponse.create(HttpStatusCode.valueOf(200))
            .body(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}")
            .build();

    final ClientResponse errorResponse =
        ClientResponse.create(HttpStatusCode.valueOf(400)).body("[]").build();

    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.just(errorResponse))
        .thenReturn(Mono.just(clientResponse));

    // Act
    final String result = testee.getPriceForItem(new Item("Test Item", List.of())).block();

    // Assert
    assertThat(result)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");
    verify(exchangeFunction, times(2)).exchange(any());
  }

  @Test
  void shouldRetryOn5xxError() {
    // Arrange
    final ClientResponse successResponse =
        ClientResponse.create(HttpStatusCode.valueOf(200))
            .body(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}")
            .build();
    final ClientResponse errorResponse =
        ClientResponse.create(HttpStatusCode.valueOf(500)).body("").build();
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.just(errorResponse))
        .thenReturn(Mono.just(successResponse));

    // Act
    final String result = testee.getPriceForItem(new Item("Test Item", List.of())).block();

    // Assert
    assertThat(result)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");
    verify(exchangeFunction, times(2)).exchange(any());
  }

  @Test
  void shouldThrowExceptionAfterRetries() {
    // Arrange
    final ClientResponse errorResponse =
        ClientResponse.create(HttpStatusCode.valueOf(500)).body("").build();
    when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(errorResponse));

    // Act
    final Mono<String> result = testee.getPriceForItem(new Item("Test Item", List.of()));

    // Assert
    assertThatThrownBy(result::block).isInstanceOf(IllegalStateException.class);
    verify(exchangeFunction, times(4)).exchange(any());
  }
}
