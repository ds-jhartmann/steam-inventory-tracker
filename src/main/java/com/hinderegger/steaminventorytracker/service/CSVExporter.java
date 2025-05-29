package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for exporting item data to CSV format.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CSVExporter {
  private final PriceService priceService;

  /**
   * Creates a CSV string from a list of items.
   *
   * @param items The items to include in the CSV
   * @return A CSV-formatted string
   */
  public String createCSV(final List<Item> items) {
    final List<String> result = new ArrayList<>();
    result.add("name,price,median");
    items.forEach(
        item -> {
          final String join = createCSVRowForItem(item);
          result.add(join);
        });
    return String.join(",\n", result);
  }

  /**
   * Creates a CSV row for a single item.
   *
   * @param item The item to create a row for
   * @return A CSV-formatted row
   */
  private String createCSVRowForItem(final Item item) {
    String latestPrice;
    String medianPrice;
    try {
      Price price = priceService.getLatestPrice(item);
      latestPrice = (price.price() + "€").replace(".", ",");
      medianPrice = (price.median() + "€").replace(".", ",");
    } catch (PriceHistoryException e) {
      log.error("Exception while parsing Price for item {}: {}", item.getItemName(), e.getMessage());
      latestPrice = "0,00€";
      medianPrice = "0,00€";
    }
    return String.join(
        ",", item.getItemName(), "\"" + latestPrice + "\"", "\"" + medianPrice + "\"");
  }
}
