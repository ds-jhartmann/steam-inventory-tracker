package com.hinderegger.steaminventorytracker.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class BuyInfo {
  @Id private String id;
  private String itemName;
  private int amount;
  private double buyPrice;

  public BuyInfo(String itemName, int amount, double buyPrice) {
    this.itemName = itemName;
    this.amount = amount;
    this.buyPrice = buyPrice;
  }
}
