package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.service.CSVExporter;
import com.hinderegger.steaminventorytracker.service.PriceHistoryException;
import com.hinderegger.steaminventorytracker.service.PriceService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CSVExporterTest {

  private PriceService priceService;
  private CSVExporter csvExporter;

  @BeforeEach
  void setUp() throws PriceHistoryException {
    priceService = mock(PriceService.class);
    csvExporter = new CSVExporter(priceService);

    // Set up default behavior for the mock
    when(priceService.getLatestPrice(any())).thenAnswer(invocation -> {
      Item item = invocation.getArgument(0);
      if (item.getPriceHistory().isEmpty()) {
        throw new PriceHistoryException("No price history for Item: " + item.getItemName());
      }
      return item.getPriceHistory().stream()
          .max((p1, p2) -> p1.timestamp().compareTo(p2.timestamp()))
          .orElseThrow();
    });
  }

  @Test
  void shouldReturnHeaderIfItemsAreEmpty() {
    // Arrange
    List<Item> items = List.of();
    // Act
    String csv = csvExporter.createCSV(items);
    // Assert
    assertThat(csv).isEqualTo("name,price,median");
  }

  @Test
  void shouldReturnItemWithDefaultPrice() {
    // Arrange
    List<Item> items = List.of(new Item("Item 1", List.of()));
    // Act
    String csv = csvExporter.createCSV(items);
    // Assert
    assertThat(csv).isEqualTo("""
      name,price,median,
      Item 1,"0,00€","0,00€\"""");
  }

  @Test
  void shouldReturnItemWithOnlyPrice() throws PriceHistoryException {
    // Arrange
    Price price = new Price(0.1, 0.2, LocalDateTime.parse("2023-12-20T12:59:59.999"));
    List<Price> priceHistory = new ArrayList<>(List.of(price));
    Item item = new Item("Item 1", priceHistory);
    List<Item> items = List.of(item);
    // Act
    String csv = csvExporter.createCSV(items);
    // Assert
    assertThat(csv).isEqualTo("""
      name,price,median,
      Item 1,"0,1€","0,2€\"""");
  }

  @Test
  void shouldReturnItemWithLatestPrice() throws PriceHistoryException {
    // Arrange
    Price price1 = new Price(0.1, 0.2, LocalDateTime.parse("2023-12-20T12:59:59.999"));
    Price price2 = new Price(0.2, 0.3, LocalDateTime.parse("2023-12-20T13:00:00.000"));
    List<Price> priceHistory = new ArrayList<>(List.of(price1, price2));
    Item item = new Item("Item 1", priceHistory);
    List<Item> items = List.of(item);
    // Act
    String csv = csvExporter.createCSV(items);
    // Assert
    assertThat(csv).isEqualTo("""
      name,price,median,
      Item 1,"0,2€","0,3€\"""");
  }

  @Test
  void shouldReturnItemsWithLatestPrice() throws PriceHistoryException {
    // Arrange
    Price price1 = new Price(0.1, 0.2, LocalDateTime.parse("2023-12-20T12:59:59.999"));
    Price price2 = new Price(0.2, 0.3, LocalDateTime.parse("2023-12-20T13:00:00.000"));
    List<Price> priceHistory1 = new ArrayList<>(List.of(price1, price2));
    Price price21 = new Price(0.3, 0.4, LocalDateTime.parse("2023-12-20T11:59:59.999"));
    Price price22 = new Price(0.4, 0.5, LocalDateTime.parse("2023-12-20T13:00:01.000"));
    List<Price> priceHistory2 = new ArrayList<>(List.of(price21, price22));
    Item item1 = new Item("Item 1", priceHistory1);
    Item item2 = new Item("Item 2", priceHistory2);
    List<Item> items = List.of(item1, item2);
    // Act
    String csv = csvExporter.createCSV(items);
    // Assert
    assertThat(csv)
        .isEqualTo(
            """
      name,price,median,
      Item 1,"0,2€","0,3€",
      Item 2,"0,4€","0,5€\"""");
  }
}
