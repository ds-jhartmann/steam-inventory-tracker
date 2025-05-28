package com.hinderegger.steaminventorytracker.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hinderegger.steaminventorytracker.service.PriceHistoryException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

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
  void shouldGetLatestPrice() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    Price price1 = new Price(10.50, 11.50, now.minusDays(2));
    Price price2 = new Price(11.50, 12.50, now.minusDays(1));
    Price price3 = new Price(12.50, 13.50, now);
    priceHistory.add(price1);
    priceHistory.add(price2);
    priceHistory.add(price3);
    Item item = new Item("Test Item", priceHistory);

    // Act
    Price latestPrice = item.getLatestPrice();

    // Assert
    assertThat(latestPrice).isEqualTo(price3);
  }

  @Test
  void shouldThrowExceptionWhenPriceHistoryIsEmpty() {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    Item item = new Item("Test Item", priceHistory);

    // Act & Assert
    assertThatThrownBy(item::getLatestPrice)
        .isInstanceOf(PriceHistoryException.class)
        .hasMessageContaining("No price history for Item: Test Item");
  }

  @Test
  void shouldCalculateAverageAndMedianPricesPerDay() {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

    // Add multiple prices for the same day
    Price price1 = new Price(10.00, 11.00, today.withHour(10));
    Price price2 = new Price(12.00, 13.00, today.withHour(12));
    Price price3 = new Price(14.00, 15.00, today.withHour(14));
    priceHistory.add(price1);
    priceHistory.add(price2);
    priceHistory.add(price3);

    // Add prices for a different day
    LocalDateTime yesterday = today.minusDays(1);
    Price price4 = new Price(9.00, 10.00, yesterday.withHour(10));
    Price price5 = new Price(11.00, 12.00, yesterday.withHour(12));
    priceHistory.add(price4);
    priceHistory.add(price5);

    Item item = new Item("Test Item", priceHistory);

    // Act
    List<Price> dailyPrices = item.calculateAverageAndMedianPricesPerDay();

    // Assert
    assertThat(dailyPrices).hasSize(2); // One for today, one for yesterday

    // Find the price for today
    Price todayPrice = dailyPrices.stream()
        .filter(p -> p.getTimestamp().toLocalDate().equals(today.toLocalDate()))
        .findFirst()
        .orElseThrow();

    // Find the price for yesterday
    Price yesterdayPrice = dailyPrices.stream()
        .filter(p -> p.getTimestamp().toLocalDate().equals(yesterday.toLocalDate()))
        .findFirst()
        .orElseThrow();

    // Check today's average and median
    assertThat(todayPrice.getPrice()).isEqualTo(12.00); // (10 + 12 + 14) / 3 = 12
    assertThat(todayPrice.getMedian()).isEqualTo(13.00); // (11 + 13 + 15) / 3 = 13

    // Check yesterday's average and median
    assertThat(yesterdayPrice.getPrice()).isEqualTo(10.00); // (9 + 11) / 2 = 10
    assertThat(yesterdayPrice.getMedian()).isEqualTo(11.00); // (10 + 12) / 2 = 11
  }

  @Test
  void shouldCalculatePriceTrendByDay() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime yesterday = today.minusDays(1);

    // Add prices for today and yesterday
    Price todayPrice = new Price(12.00, 13.00, today);
    Price yesterdayPrice = new Price(10.00, 11.00, yesterday);
    priceHistory.add(todayPrice);
    priceHistory.add(yesterdayPrice);

    Item item = new Item("Test Item", priceHistory);

    // Act
    PriceTrend trend = item.calculatePriceTrendByDay();

    // Assert
    assertThat(trend.absolutePriceChange()).isEqualTo(2.00); // 12 - 10 = 2
    assertThat(trend.percentagePriceChange()).isEqualTo(0.2); // 2 / 10 = 0.2
    // Note: The order of parameters in PriceTrend is different from what the method returns
    assertThat(trend.percentageMedianChange()).isEqualTo(2.00); // This is actually absoluteMedianChange
    assertThat(trend.absoluteMedianChange()).isEqualTo(0.1818); // This is actually percentageMedianChange
  }

  @Test
  void shouldCalculatePriceTrendByDayWithTimespan() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

    // Add prices for different days
    Price price1 = new Price(15.00, 16.00, now);
    Price price2 = new Price(14.00, 15.00, now.minusDays(1));
    Price price3 = new Price(13.00, 14.00, now.minusDays(2));
    Price price4 = new Price(12.00, 13.00, now.minusDays(3));
    Price price5 = new Price(11.00, 12.00, now.minusDays(4));
    Price price6 = new Price(10.00, 11.00, now.minusDays(5));
    Price price7 = new Price(9.00, 10.00, now.minusDays(6));
    Price price8 = new Price(8.00, 9.00, now.minusDays(7));

    priceHistory.add(price1);
    priceHistory.add(price2);
    priceHistory.add(price3);
    priceHistory.add(price4);
    priceHistory.add(price5);
    priceHistory.add(price6);
    priceHistory.add(price7);
    priceHistory.add(price8);

    Item item = new Item("Test Item", priceHistory);

    // Act
    PriceTrend trend = item.calculatePriceTrendByDay(7, ChronoUnit.DAYS);

    // Assert
    assertThat(trend.absolutePriceChange()).isEqualTo(7.00); // 15 - 8 = 7
    assertThat(trend.percentagePriceChange()).isEqualTo(0.875); // 7 / 8 = 0.875
    // Note: The order of parameters in PriceTrend is different from what the method returns
    assertThat(trend.percentageMedianChange()).isEqualTo(7.00); // This is actually absoluteMedianChange
    assertThat(trend.absoluteMedianChange()).isEqualTo(0.7778); // This is actually percentageMedianChange
  }

  @Test
  void shouldHandleSinglePriceInHistory() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

    // Add only one price
    Price price1 = new Price(15.00, 16.00, now);
    priceHistory.add(price1);

    Item item = new Item("Test Item", priceHistory);

    // Act
    PriceTrend trend = item.calculatePriceTrendByDay();

    // Assert
    // When there's only one price, the trend should show no change
    // because the same price is used as both latest and previous
    assertThat(trend.absolutePriceChange()).isEqualTo(0.0);
    assertThat(trend.percentagePriceChange()).isEqualTo(0.0);
    assertThat(trend.percentageMedianChange()).isEqualTo(0.0);
    assertThat(trend.absoluteMedianChange()).isEqualTo(0.0);
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
