package com.hinderegger.steaminventorytracker;

import com.hinderegger.steaminventorytracker.controller.ItemQueryException;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import java.util.Comparator;
import java.util.List;

public class PriceParser {
  private PriceParser() {}

  public static Price getLatestPriceFromItem(final Item itemByName) throws ItemQueryException {
    final List<Price> priceHistory = itemByName.getPriceHistory();
    if (!priceHistory.isEmpty()) {
      priceHistory.sort(Comparator.comparing(Price::getTimestamp));
      final int lastIndex = priceHistory.size() - 1;
      return priceHistory.get(lastIndex);
    } else {
      throw new ItemQueryException("No price history for Item: " + itemByName.getItemName());
    }
  }
}
