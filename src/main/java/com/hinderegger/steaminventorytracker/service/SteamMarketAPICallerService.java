package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.SteamInventoryTrackerApplication;
import com.hinderegger.steaminventorytracker.model.Item;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class SteamMarketAPICallerService {

    @Resource(name = "steamWebClient")
    private WebClient client;

    @Resource(name = "steamRateLimiter")
    private RateLimiter rateLimiter;

    @Value("${steam.path}")
    private String path;

    public Mono<String> getPriceForItem(Item item) {
        return getFromApi(item.getItemName());
    }

    public Mono<String> getFromApi(String itemName) {
        log.info("Starting api request for item: " + itemName);

        return client
                .get()
                .uri(path + itemName)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(s -> log.info(SteamInventoryTrackerApplication.COUNTER.incrementAndGet() + " - " + LocalDateTime.now()
                        + " - call triggered"))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(30L)).filter(this::is5xxServerError));
    }

    private boolean is5xxServerError(Throwable throwable) {
        return ((WebClientResponseException) throwable).getStatusCode().is4xxClientError();
    }
}
