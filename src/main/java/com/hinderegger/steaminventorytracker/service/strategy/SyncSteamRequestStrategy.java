package com.hinderegger.steaminventorytracker.service.strategy;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.SteamMarketHttpClient;
import com.hinderegger.steaminventorytracker.service.TimeProvider;
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

  private final SteamConfiguration steamConfig;
  private final SteamMarketHttpClient steamMarketHttpClient;

  public SyncSteamRequestStrategy(
      SteamConfiguration steamConfig,
      TimeProvider timeProvider,
      ItemService itemService,
      final SteamMarketHttpClient steamMarketHttpClient) {
    super(itemService, timeProvider);
    this.steamConfig = steamConfig;
    this.steamMarketHttpClient = steamMarketHttpClient;
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

    try {
      final String response = callSteamAPI(item);
      final JSONObject jsonObject = new JSONObject(response);
      parseAndStoreItem(item, jsonObject);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (SteamApiQueryException e) {
      log.error("Could not get Item: {}. Reason: {}", item.getItemName(), e.getMessage());
    }

    try {
      timeProvider.sleep(steamConfig.getSleepDuration());
    } catch (final InterruptedException e) {
      log.error("Interrupt exception thrown: {}", e.getMessage());
      Thread.currentThread().interrupt();
    }
  }

  private String callSteamAPI(Item item) throws InterruptedException, SteamApiQueryException {
    return steamMarketHttpClient.getStringHttpResponse(item);
  }
}
