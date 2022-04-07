package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.SteamInventoryTrackerApplication;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SteamInventoryTrackerService {
    private final ItemRepository itemRepository;
    private final SteamMarketAPICallerService steamMarketAPICallerService;

    public SteamInventoryTrackerService(ItemRepository itemRepository, SteamMarketAPICallerService steamMarketAPICallerService) {
        this.itemRepository = itemRepository;
        this.steamMarketAPICallerService = steamMarketAPICallerService;
    }

    public void requestItems() {
        final List<Item> all = itemRepository.findAll();
        Collections.shuffle(all);
        final long started = System.currentTimeMillis();
        all.forEach(this::requestItem);
        log.info("Elapsed time in seconds: " + (System.currentTimeMillis() - started) / 1000d);
    }

    public void requestItemsSync() {
        final List<Item> all = itemRepository.findAll();
        final long started = System.currentTimeMillis();
        final HttpClient httpClient = HttpClient.newHttpClient();
        all.forEach(item -> requestItemSync(httpClient, item));
        log.info("Elapsed time in minutes: " + (System.currentTimeMillis() - started) / 1000d / 60d);
    }

    private void requestItemSync(HttpClient httpClient, Item item) {
        String url = SteamInventoryTrackerApplication.PATH +
                URLEncoder.encode(item.getItemName(), StandardCharsets.UTF_8);
        log.info(url);
        final HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .uri(URI.create(SteamInventoryTrackerApplication.BASEURL + url))
                .GET()
                .build();
        try {
            final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                final JSONObject jsonObject = new JSONObject(response.body());
                parseAndStoreItem(item, jsonObject);
            } else {
                log.error("Could not get Item: " + item.getItemName() + ". Reason: " + response.statusCode());
            }
            TimeUnit.SECONDS.sleep(15);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private void parseAndStoreItem(Item item, JSONObject jsonObject) {
        try {
            final double median_price = Double.parseDouble(formatString(jsonObject, "median_price"));
            final double lowest_price = Double.parseDouble(formatString(jsonObject, "lowest_price"));

            if (lowest_price > 0.0) {
                final Price price = new Price(lowest_price, median_price, LocalDateTime.now());
                log.info("Adding Item: " + price);
                item.addPrice(price);
                itemRepository.save(item);
            } else {
                log.error("No lowest_price found. Skipping Item: " + item.getItemName());
            }
        } catch (Exception e) {
            log.error("Error while accessing Response JSON. Skipping Item: " + item.getItemName());
            log.error(e.getMessage());
        }
    }

    private String formatString(JSONObject jsonObject, String key) {
        return jsonObject.getString(key).replace("€", "").replace(",", ".").replace("-", "0");
    }

    private void requestItem(Item item) {
        final Mono<String> priceMono = callSteamAPI(item);

        priceMono.log().subscribe(priceResponse -> {
            log.info("Price is: " + priceResponse);
            final JSONObject jsonObject = new JSONObject(priceResponse);
            parseAndStoreItem(item, jsonObject);
        }, error -> {
            log.error("Could not get price");
            throw new IllegalStateException("error while retrieving value for item: " + item.getItemName());
        }, () -> log.info("Mono consumed."));
    }

    private Mono<String> callSteamAPI(Item item) {
        return steamMarketAPICallerService.getPriceForItem(item);
    }
}
