package com.hinderegger.steaminventorytracker.persistence;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buy_infos")
@Getter
@Setter
@NoArgsConstructor
public class BuyInfoEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "item_name", nullable = false)
  private String itemName;

  @Column(nullable = false)
  private int amount;

  @Column(name = "buy_price", nullable = false)
  private double buyPrice;

  public BuyInfoEntity(String itemName, int amount, double buyPrice) {
    this.itemName = itemName;
    this.amount = amount;
    this.buyPrice = buyPrice;
  }
}
