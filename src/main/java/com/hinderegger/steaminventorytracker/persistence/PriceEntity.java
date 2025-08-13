package com.hinderegger.steaminventorytracker.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
public class PriceEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_name", nullable = false)
  private ItemEntity item;

  @Column(nullable = false)
  private double price;

  @Column(nullable = false)
  private double median;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  public PriceEntity(ItemEntity item, double price, double median, LocalDateTime timestamp) {
    this.item = item;
    this.price = price;
    this.median = median;
    this.timestamp = timestamp;
  }
}
