package com.hinderegger.steaminventorytracker.persistence;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class ItemEntity {
  @Id
  @Column(name = "item_name", nullable = false)
  private String itemName;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<PriceEntity> priceHistory = new ArrayList<>();

  public ItemEntity(String itemName) {
    this.itemName = itemName;
  }
}
