package com.hinderegger.steaminventorytracker.service.strategy;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.service.SteamMarketAPIClient;
import com.hinderegger.steaminventorytracker.service.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Asynchronous implementation of the SteamRequestStrategy.
 * This strategy processes items using reactive programming for non-blocking operations.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncSteamRequestStrategy implements SteamRequestStrategy {

    private final SteamMarketAPIClient steamMarketAPIClient;
    private final ItemRepository itemRepository;
    private final TimeProvider timeProvider;

    @Override
    public void requestItems(List<Item> items) {
        // Create a shuffled copy to avoid hitting rate limits for the same items
        List<Item> shuffledItems = new java.util.ArrayList<>(items);
        Collections.shuffle(shuffledItems);
        
        final long started = timeProvider.currentTimeMillis();
        shuffledItems.forEach(this::requestItem);
        log.info("Async request initiated. Elapsed setup time in seconds: {}", 
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
                error -> {
                    log.error("Error getting price for item {}: {}", 
                            item.getItemName(), error.getMessage());
                },
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

    /**
     * Parses the JSON response and stores the price data in the item.
     *
     * @param item The item to update
     * @param jsonObject The JSON response from Steam
     */
    private void parseAndStoreItem(Item item, JSONObject jsonObject) {
        try {
            double medianPrice = 0;
            double lowestPrice = 0;
            if (jsonObject.has("median_price")) {
                medianPrice = Double.parseDouble(formatString(jsonObject, "median_price"));
            }
            if (jsonObject.has("lowest_price")) {
                lowestPrice = Double.parseDouble(formatString(jsonObject, "lowest_price"));
            }

            if (lowestPrice > 0.0) {
                final Price price = new Price(lowestPrice, medianPrice, timeProvider.now());
                log.info("Adding price {} to item {}", price, item.getItemName());
                item.addPrice(price);
                itemRepository.save(item);
            } else {
                log.error("No lowest_price found. Skipping Item: {}", item.getItemName());
            }
        } catch (NumberFormatException e) {
            log.error("Error while parsing response JSON for item {}: {}", 
                    item.getItemName(), e.getMessage());
        }
    }

    /**
     * Formats a string value from the JSON response for parsing as a number.
     *
     * @param jsonObject The JSON object
     * @param key The key to extract
     * @return The formatted string
     */
    private String formatString(JSONObject jsonObject, String key) {
        return jsonObject
                .getString(key)
                .replace("€", "")
                .replace(",", ".")
                .replace(" ", "")
                .replace("-", "0");
    }
}