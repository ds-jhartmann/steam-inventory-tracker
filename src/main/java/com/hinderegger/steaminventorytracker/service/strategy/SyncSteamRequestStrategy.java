package com.hinderegger.steaminventorytracker.service.strategy;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.service.TimeProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Synchronous implementation of the SteamRequestStrategy. This strategy processes items one by one
 * in a blocking manner.
 */
@Component
@Slf4j
public class SyncSteamRequestStrategy extends AbstractSteamRequestStrategy {

  private final HttpClient httpClient;
  private final SteamConfiguration steamConfig;

  public SyncSteamRequestStrategy(
      HttpClient httpClient,
      SteamConfiguration steamConfig,
      TimeProvider timeProvider,
      ItemRepository itemRepository) {
    super(itemRepository, timeProvider);
    this.httpClient = httpClient;
    this.steamConfig = steamConfig;
  }

  @Override
  public void requestItems(List<Item> items) {
    final long started = timeProvider.currentTimeMillis();
    items.forEach(this::requestItem);
    log.info(
        "Elapsed time in minutes: {}", (timeProvider.currentTimeMillis() - started) / 1000d / 60d);
  }

  /**
   * Requests data for a single item from Steam.
   *
   * @param item The item to request data for
   */
  private void requestItem(Item item) {
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
        log.error("Could not get Item: {}. Reason: {}", item.getItemName(), response.statusCode());
      }
      timeProvider.sleep(steamConfig.getSleepDuration());
    } catch (final IOException e) {
      log.error("Error while requesting Item: {}", e.getMessage());
    } catch (final InterruptedException e) {
      log.error("Interrupt exception thrown: {}", e.getMessage());
      Thread.currentThread().interrupt();
    }
  }
}
