package com.hinderegger.steaminventorytracker.model;

import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents an item in the Steam inventory.
 * This class is a simple data model without business logic, following the Single Responsibility Principle.
 * Business logic has been moved to appropriate service classes.
 */
@Data
@Document
public class Item {
  @Id @Indexed private final String itemName;
  private final List<Price> priceHistory;

  /**
   * Adds a price to the item's price history.
   *
   * @param price The price to add
   */
  public void addPrice(final Price price) {
    priceHistory.add(price);
  }
}
