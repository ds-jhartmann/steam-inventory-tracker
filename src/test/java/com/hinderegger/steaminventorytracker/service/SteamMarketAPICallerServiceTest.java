package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hinderegger.steaminventorytracker.model.Item;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
class SteamMarketAPICallerServiceTest {

  private SteamMarketAPICallerService testee;
  @Mock private ExchangeFunction exchangeFunction;

  @BeforeEach
  void setUp() {
    final WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();

    final RateLimiter rateLimiter =
        RateLimiter.of(
            "test",
            RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(2L))
                .limitForPeriod(1)
                .timeoutDuration(Duration.ofSeconds(6L))
                .build());
    testee = new SteamMarketAPICallerService(webClient, rateLimiter, "?market_hash_name=");
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
    final String result2 = testee.getPriceForItem(new Item("Test Item", List.of())).block();
    test.stop();

    // Assert
    assertThat(test.getTotalTime(TimeUnit.SECONDS)).isGreaterThan(1);
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
