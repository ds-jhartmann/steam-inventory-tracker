package com.hinderegger.steaminventorytracker;

import com.hinderegger.steaminventorytracker.model.Item;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CSVExporter {
  private CSVExporter() {}

  public static String createCSV(final List<Item> items) {

    final List<String> result = new ArrayList<>();
    result.add("name,price,median");
    items.forEach(
        item -> {
          final String join = createCSVRowForItem(item);
          result.add(join);
        });
    return String.join(",\n", result);
  }

  private static String createCSVRowForItem(final Item item) {
    String latestPrice;
    String medianPrice;
    try {
      latestPrice = (PriceParser.getLatestPriceFromItem(item).getPrice() + "€").replace(".", ",");
    } catch (Exception e) {
      log.error("Exception while parsing Price", e);
      latestPrice = "0,00€";
    }
    try {
      medianPrice = (PriceParser.getLatestPriceFromItem(item).getMedian() + "€").replace(".", ",");
    } catch (Exception e) {
      log.error("Exception while parsing Price", e);
      medianPrice = "0,00€";
    }
    return String.join(
        ",", item.getItemName(), "\"" + latestPrice + "\"", "\"" + medianPrice + "\"");
  }
}
