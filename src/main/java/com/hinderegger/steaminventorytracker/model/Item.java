package com.hinderegger.steaminventorytracker.model;

import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Item {
  @Id @Indexed private final String itemName;
  private final List<Price> priceHistory;

  public void addPrice(final Price price) {
    priceHistory.add(price);
  }
}
