package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.hinderegger.steaminventorytracker.controller.ItemQueryException;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceParserTest {

  @Test
  void shouldReturnOnlyPrice() throws ItemQueryException {
    // Arrange
    Price price = new Price(0.1, 0.2, LocalDateTime.parse("2023-12-20T12:59:59.999"));
    List<Price> priceHistory = new ArrayList<>(List.of(price));
    Item itemByName = new Item("Item 1", priceHistory);

    // Act
    Price latestPriceFromItem = PriceParser.getLatestPriceFromItem(itemByName);

    // Assert
    assertThat(latestPriceFromItem).isEqualTo(price);
  }

  @Test
  void shouldReturnLatestPrice() throws ItemQueryException {
    // Arrange
    Price price1 = new Price(0.1, 0.2, LocalDateTime.parse("2023-12-20T12:59:59.999"));
    Price price2 = new Price(0.2, 0.3, LocalDateTime.parse("2023-12-20T13:00:00.000"));
    List<Price> priceHistory = new ArrayList<>(List.of(price1, price2));
    Item itemByName = new Item("Item 1", priceHistory);

    // Act
    Price latestPriceFromItem = PriceParser.getLatestPriceFromItem(itemByName);

    // Assert
    assertThat(latestPriceFromItem).isEqualTo(price2);
  }

  @Test
  void shouldThrowExceptionForNoPriceHistory() {
    // Arrange
    List<Price> priceHistory = new ArrayList<>(List.of());
    Item itemByName = new Item("Item 1", priceHistory);

    // Act & Assert
    assertThatThrownBy(() -> PriceParser.getLatestPriceFromItem(itemByName))
        .isInstanceOf(ItemQueryException.class);
  }
}
