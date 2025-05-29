package com.hinderegger.steaminventorytracker.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Item model class.
 * Since business logic has been moved to the PriceService, these tests focus on the basic
 * functionality of the Item class.
 */
class ItemTest {

  @Test
  void shouldCreateItemWithConstructor() {
    // Arrange & Act
    List<Price> priceHistory = new ArrayList<>();
    Item item = new Item("Test Item", priceHistory);

    // Assert
    assertThat(item.getItemName()).isEqualTo("Test Item");
    assertThat(item.getPriceHistory()).isSameAs(priceHistory);
  }

  @Test
  void shouldAddPriceToHistory() {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    Item item = new Item("Test Item", priceHistory);
    Price price = new Price(10.50, 11.50, LocalDateTime.now());

    // Act
    item.addPrice(price);

    // Assert
    assertThat(item.getPriceHistory()).contains(price);
    assertThat(item.getPriceHistory()).hasSize(1);
  }

  @Test
  void shouldImplementEqualsAndHashCode() {
    // Arrange
    List<Price> priceHistory1 = new ArrayList<>();
    List<Price> priceHistory2 = new ArrayList<>();

    Item item1 = new Item("Test Item", priceHistory1);
    Item item2 = new Item("Test Item", priceHistory2);
    Item item3 = new Item("Different Item", priceHistory1);

    // Assert
    assertThat(item1).isEqualTo(item2);
    assertThat(item1).isNotEqualTo(item3);
    assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
    assertThat(item1.hashCode()).isNotEqualTo(item3.hashCode());
  }

  @Test
  void shouldImplementToString() {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    Item item = new Item("Test Item", priceHistory);

    // Act
    String toString = item.toString();

    // Assert
    assertThat(toString).contains("itemName=Test Item");
    assertThat(toString).contains("priceHistory=[]");
  }
}
