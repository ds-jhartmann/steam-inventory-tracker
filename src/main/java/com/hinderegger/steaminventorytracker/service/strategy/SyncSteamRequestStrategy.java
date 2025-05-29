package com.hinderegger.steaminventorytracker.service.strategy;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.service.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Synchronous implementation of the SteamRequestStrategy.
 * This strategy processes items one by one in a blocking manner.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SyncSteamRequestStrategy implements SteamRequestStrategy {

    private final HttpClient httpClient;
    private final SteamConfiguration steamConfig;
    private final TimeProvider timeProvider;
    private final ItemRepository itemRepository;

    @Override
    public void requestItems(List<Item> items) {
        final long started = timeProvider.currentTimeMillis();
        items.forEach(this::requestItem);
        log.info("Elapsed time in minutes: {}", (timeProvider.currentTimeMillis() - started) / 1000d / 60d);
    }

    /**
     * Requests data for a single item from Steam.
     *
     * @param item The item to request data for
     */
    private void requestItem(Item item) {
        String url = steamConfig.getPath() + URLEncoder.encode(item.getItemName(), StandardCharsets.UTF_8);
        log.info(url);
        final HttpRequest httpRequest =
                HttpRequest.newBuilder().uri(URI.create(steamConfig.getBaseurl() + url)).GET().build();
        try {
            final HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                final JSONObject jsonObject = new JSONObject(response.body());
                parseAndStoreItem(item, jsonObject);
            } else {
                log.error(
                        "Could not get Item: {}. Reason: {}", item.getItemName(), response.statusCode());
            }
            timeProvider.sleep(steamConfig.getSleepDuration());
        } catch (final IOException e) {
            log.error("Error while requesting Item: {}", e.getMessage());
        } catch (final InterruptedException e) {
            log.error("Interrupt exception thrown: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
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