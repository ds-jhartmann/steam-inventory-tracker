package com.hinderegger.steaminventorytracker.service.strategy;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.TimeProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

/**
 * Abstract base class for SteamRequestStrategy implementations. Contains common functionality
 * shared between different strategy implementations.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSteamRequestStrategy implements SteamRequestStrategy {

  public static final String MEDIAN_PRICE_KEY = "median_price";
  public static final String LOWEST_PRICE_KEY = "lowest_price";
  protected final ItemService itemService;
  protected final TimeProvider timeProvider;

  @Override
  public abstract void requestItems(List<Item> items);

  /**
   * Parses the JSON response and stores the price data in the item.
   *
   * @param item The item to update
   * @param jsonObject The JSON response from Steam
   */
  protected void parseAndStoreItem(Item item, JSONObject jsonObject) {
    try {
      double medianPrice = 0;
      double lowestPrice = 0;
      if (jsonObject.has(MEDIAN_PRICE_KEY)) {
        medianPrice = Double.parseDouble(formatString(jsonObject, MEDIAN_PRICE_KEY));
      }
      if (jsonObject.has(LOWEST_PRICE_KEY)) {
        lowestPrice = Double.parseDouble(formatString(jsonObject, LOWEST_PRICE_KEY));
      }

      if (lowestPrice > 0.0) {
        log.info("Updating price for {}: lowest={}, median={}", item.getItemName(), lowestPrice, medianPrice);
        itemService.updatePriceForItem(item.getItemName(), lowestPrice, medianPrice);
      } else {
        log.error("No lowest_price found. Skipping Item: {}", item.getItemName());
      }
    } catch (NumberFormatException e) {
      log.error(
          "Error while parsing response JSON for item {}: {}", item.getItemName(), e.getMessage());
    }
  }

  /**
   * Formats a string value from the JSON response for parsing as a number.
   *
   * @param jsonObject The JSON object
   * @param key The key to extract
   * @return The formatted string
   */
  protected String formatString(JSONObject jsonObject, String key) {
    return jsonObject
        .getString(key)
        .replace("€", "")
        .replace(",", ".")
        .replace(" ", "")
        .replace("-", "0");
  }
}
