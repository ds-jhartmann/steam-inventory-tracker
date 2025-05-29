package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.model.PriceTrend;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the PriceService class. */
class PriceServiceTest {

  private PriceService priceService;
  private Item testItem;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    priceService = new PriceService();
    now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

    // Create test item with price history
    List<Price> priceHistory = new ArrayList<>();

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

    testItem = new Item("Test Item", priceHistory);
  }

  @Test
  void shouldGetLatestPrice() throws PriceHistoryException {
    // Act
    Price latestPrice = priceService.getLatestPrice(testItem);

    // Assert
    assertThat(latestPrice.price()).isEqualTo(15.00);
    assertThat(latestPrice.median()).isEqualTo(16.00);
    assertThat(latestPrice.timestamp()).isEqualTo(now);
  }

  @Test
  void shouldThrowExceptionWhenPriceHistoryIsEmpty() {
    // Arrange
    Item emptyItem = new Item("Empty Item", new ArrayList<>());

    // Act & Assert
    assertThatThrownBy(() -> priceService.getLatestPrice(emptyItem))
        .isInstanceOf(PriceHistoryException.class)
        .hasMessageContaining("No price history for Item: Empty Item");
  }

  @Test
  void shouldCalculateAverageAndMedianPricesPerDay() {
    // Act
    List<Price> dailyPrices = priceService.calculateAverageAndMedianPricesPerDay(testItem);

    // Assert
    assertThat(dailyPrices).hasSize(8); // One for each day

    // Check the latest day's price
    Price latestDayPrice =
        dailyPrices.stream()
            .filter(p -> p.timestamp().toLocalDate().equals(now.toLocalDate()))
            .findFirst()
            .orElseThrow();

    assertThat(latestDayPrice.price()).isEqualTo(15.00);
    assertThat(latestDayPrice.median()).isEqualTo(16.00);
  }

  @Test
  void shouldCalculatePriceTrendByDay() throws PriceHistoryException {
    // Act
    PriceTrend trend = priceService.calculatePriceTrendByDay(testItem);

    // Assert
    assertThat(trend.absolutePriceChange()).isEqualTo(1.00); // 15 - 14 = 1
    assertThat(trend.percentagePriceChange()).isEqualTo(0.0714); // 1 / 14 = 0.0714
    assertThat(trend.absoluteMedianChange()).isEqualTo(1.00); // 16 - 15 = 1
    assertThat(trend.percentageMedianChange()).isEqualTo(0.0667); // 1 / 15 = 0.0667
  }

  @Test
  void shouldCalculatePriceTrendByDayWithTimespan() throws PriceHistoryException {
    // Act
    PriceTrend trend = priceService.calculatePriceTrendByDay(testItem, 7, ChronoUnit.DAYS);

    // Assert
    assertThat(trend.absolutePriceChange()).isEqualTo(7.00); // 15 - 8 = 7
    assertThat(trend.percentagePriceChange()).isEqualTo(0.875); // 7 / 8 = 0.875
    assertThat(trend.absoluteMedianChange()).isEqualTo(7.00); // 16 - 9 = 7
    assertThat(trend.percentageMedianChange()).isEqualTo(0.7778); // 7 / 9 = 0.7778
  }

  @Test
  void shouldHandleSinglePriceInHistory() throws PriceHistoryException {
    // Arrange
    List<Price> singlePriceHistory = new ArrayList<>();
    Price singlePrice = new Price(15.00, 16.00, now);
    singlePriceHistory.add(singlePrice);
    Item singlePriceItem = new Item("Single Price Item", singlePriceHistory);

    // Act
    PriceTrend trend = priceService.calculatePriceTrendByDay(singlePriceItem);

    // Assert
    // When there's only one price, the trend should show no change
    // because the same price is used as both latest and previous
    assertThat(trend.absolutePriceChange()).isEqualTo(0.0);
    assertThat(trend.percentagePriceChange()).isEqualTo(0.0);
    assertThat(trend.absoluteMedianChange()).isEqualTo(0.0);
    assertThat(trend.percentageMedianChange()).isEqualTo(0.0);
  }

  @Test
  void shouldThrowExceptionWhenNoDataForTimespan() {
    // Arrange
    // Create an item with price history that doesn't go back far enough
    List<Price> limitedPriceHistory = new ArrayList<>();
    limitedPriceHistory.add(new Price(15.00, 16.00, now));
    limitedPriceHistory.add(new Price(14.00, 15.00, now.minusDays(1)));
    Item itemWithLimitedHistory = new Item("Limited History Item", limitedPriceHistory);

    // Act & Assert
    // Try to get price trend for 30 days, which should fail because we only have 2 days of history
    assertThatThrownBy(
            () ->
                priceService.calculatePriceTrendByDay(itemWithLimitedHistory, 30, ChronoUnit.DAYS))
        .isInstanceOf(PriceHistoryException.class)
        .hasMessageContaining("There is no Price within 30")
        .hasMessageContaining("prior to the latest Price.");
  }
}
