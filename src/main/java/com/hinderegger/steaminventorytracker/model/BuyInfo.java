package com.hinderegger.steaminventorytracker.model;

import lombok.Data;

@Data
public class BuyInfo {
  private String id;
  private String itemName;
  private int amount;
  private double buyPrice;

  public BuyInfo(String itemName, int amount, double buyPrice) {
    this.itemName = itemName;
    this.amount = amount;
    this.buyPrice = buyPrice;
  }
}
