package com.hinderegger.steaminventorytracker.service.strategy;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.SteamMarketAPIClient;
import com.hinderegger.steaminventorytracker.service.TimeProvider;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Asynchronous implementation of the SteamRequestStrategy. This strategy processes items using
 * reactive programming for non-blocking operations.
 */
@Component
@Slf4j
public class AsyncSteamRequestStrategy extends AbstractSteamRequestStrategy {

  private final SteamMarketAPIClient steamMarketAPIClient;

  public AsyncSteamRequestStrategy(
      SteamMarketAPIClient steamMarketAPIClient,
      ItemService itemService,
      TimeProvider timeProvider) {
    super(itemService, timeProvider);
    this.steamMarketAPIClient = steamMarketAPIClient;
  }

  @Override
  public void requestItems(List<Item> items) {
    // Create a shuffled copy to avoid hitting rate limits for the same items
    List<Item> shuffledItems = new java.util.ArrayList<>(items);
    Collections.shuffle(shuffledItems);

    final long started = timeProvider.currentTimeMillis();
    shuffledItems.forEach(this::requestItem);
    log.info(
        "Async request initiated. Elapsed setup time in seconds: {}",
        (timeProvider.currentTimeMillis() - started) / 1000d);
  }

  /**
   * Requests data for a single item from Steam asynchronously.
   *
   * @param item The item to request data for
   */
  private void requestItem(Item item) {
    final Mono<String> priceMono = callSteamAPI(item);

    priceMono
        .log()
        .subscribe(
            priceResponse -> {
              log.info("Received price for item {}: {}", item.getItemName(), priceResponse);
              final JSONObject jsonObject = new JSONObject(priceResponse);
              parseAndStoreItem(item, jsonObject);
            },
            error ->
                log.error(
                    "Error getting price for item {}: {}", item.getItemName(), error.getMessage()),
            () -> log.info("Request completed for item: {}", item.getItemName()));
  }

  /**
   * Calls the Steam API to get price data for an item.
   *
   * @param item The item to get price data for
   * @return A Mono containing the response
   */
  private Mono<String> callSteamAPI(Item item) {
    return steamMarketAPIClient.getPriceForItem(item);
  }
}
