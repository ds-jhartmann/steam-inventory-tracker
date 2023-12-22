package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class SteamInventoryTrackerService {
  private final ItemRepository itemRepository;
  private final SteamMarketAPICallerService steamMarketAPICallerService;
  private final SteamConfiguration steamConfig;
  private final HttpClient httpClient;

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
    all.forEach(item -> requestItemSync(httpClient, item));
    log.info("Elapsed time in minutes: " + (System.currentTimeMillis() - started) / 1000d / 60d);
  }

  private void requestItemSync(HttpClient httpClient, Item item) {
    String url =
        steamConfig.getPath() + URLEncoder.encode(item.getItemName(), StandardCharsets.UTF_8);
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
            "Could not get Item: " + item.getItemName() + ". Reason: " + response.statusCode());
      }
      TimeUnit.SECONDS.sleep(steamConfig.getSleepDuration());
    } catch (final IOException e) {
      log.error("Error while requesting Item.", e);
    } catch (final InterruptedException e) {
      log.error("Interrupt exception thrown.", e);
      Thread.currentThread().interrupt();
    }
  }

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
        final Price price = new Price(lowestPrice, medianPrice, LocalDateTime.now());
        log.info("Adding Item: " + price);
        item.addPrice(price);
        itemRepository.save(item);
      } else {
        log.error("No lowest_price found. Skipping Item: " + item.getItemName());
      }
    } catch (NumberFormatException e) {
      log.error("Error while accessing Response JSON. Skipping Item: " + item.getItemName());
      log.error(e.getMessage());
    }
  }

  private String formatString(JSONObject jsonObject, String key) {
    return jsonObject
        .getString(key)
        .replace("€", "")
        .replace(",", ".")
        .replace(" ", "")
        .replace("-", "0");
  }

  private void requestItem(Item item) {
    final Mono<String> priceMono = callSteamAPI(item);

    priceMono
        .log()
        .subscribe(
            priceResponse -> {
              log.info("Price is: " + priceResponse);
              final JSONObject jsonObject = new JSONObject(priceResponse);
              parseAndStoreItem(item, jsonObject);
            },
            error -> {
              log.error("Could not get price");
              throw new IllegalStateException(
                  "error while retrieving value for item: " + item.getItemName());
            },
            () -> log.info("Mono consumed."));
  }

  private Mono<String> callSteamAPI(Item item) {
    return steamMarketAPICallerService.getPriceForItem(item);
  }
}
