package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hinderegger.steaminventorytracker.model.Item;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SteamMarketAPICallerServiceTest {

  private SteamMarketAPICallerService testee;
  @Mock private ExchangeFunction exchangeFunction;

  @BeforeEach
  void setUp() {
    WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();

    RateLimiter rateLimiter = RateLimiter.ofDefaults("test");
    testee = new SteamMarketAPICallerService(webClient, rateLimiter, "?market_hash_name=");
  }

  @Test
  void shouldReturnPriceOnSuccess() {
    ClientResponse clientResponse =
        ClientResponse.create(HttpStatusCode.valueOf(200))
            .body(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}")
            .build();
    when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(clientResponse));

    String result = testee.getPriceForItem(new Item("Test Item", List.of())).block();

    assertThat(result)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");
    verify(exchangeFunction, times(1)).exchange(any());
  }

  @Test
  void shouldRetryOn4xxError() {
    ClientResponse clientResponse =
        ClientResponse.create(HttpStatusCode.valueOf(200))
            .body(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}")
            .build();

    ClientResponse errorResponse =
        ClientResponse.create(HttpStatusCode.valueOf(400)).body("[]").build();

    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.just(errorResponse))
        .thenReturn(Mono.just(clientResponse));

    String result = testee.getPriceForItem(new Item("Test Item", List.of())).block();

    assertThat(result)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");
    verify(exchangeFunction, times(2)).exchange(any());
  }

  @Test
  void shouldRetryOn5xxError() {
    ClientResponse successResponse =
        ClientResponse.create(HttpStatusCode.valueOf(200))
            .body(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}")
            .build();
    ClientResponse errorResponse =
        ClientResponse.create(HttpStatusCode.valueOf(500)).body("").build();

    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.just(errorResponse))
        .thenReturn(Mono.just(successResponse));

    String result = testee.getPriceForItem(new Item("Test Item", List.of())).block();

    assertThat(result)
        .isEqualTo(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");
    verify(exchangeFunction, times(2)).exchange(any());
  }

  @Test
  void shouldThrowExceptionAfterRetries() {
    ClientResponse errorResponse =
        ClientResponse.create(HttpStatusCode.valueOf(500)).body("").build();

    when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(errorResponse));

    Mono<String> result = testee.getPriceForItem(new Item("Test Item", List.of()));

    assertThatThrownBy(result::block).isInstanceOf(IllegalStateException.class);

    verify(exchangeFunction, times(4)).exchange(any());
  }
}
