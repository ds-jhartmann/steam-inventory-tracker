package com.hinderegger.steaminventorytracker.model;

import static org.assertj.core.api.Assertions.*;

import com.hinderegger.steaminventorytracker.service.PriceHistoryException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceTest {

  @Test
  void shouldReturnOnlyPrice() throws PriceHistoryException {
    // Arrange
    Price price = new Price(0.1, 0.2, LocalDateTime.parse("2023-12-20T12:59:59.999"));
    List<Price> priceHistory = new ArrayList<>(List.of(price));
    Item item = new Item("Item 1", priceHistory);

    // Act
    Price latestPriceFromItem = item.getLatestPrice();

    // Assert
    assertThat(latestPriceFromItem).isEqualTo(price);
  }

  @Test
  void shouldReturnLatestPrice() throws PriceHistoryException {
    // Arrange
    Price price1 = new Price(0.1, 0.2, LocalDateTime.parse("2023-12-20T12:59:59.999"));
    Price price2 = new Price(0.2, 0.3, LocalDateTime.parse("2023-12-20T13:00:00.000"));
    List<Price> priceHistory = new ArrayList<>(List.of(price1, price2));
    Item item = new Item("Item 1", priceHistory);

    // Act
    Price latestPriceFromItem = item.getLatestPrice();

    // Assert
    assertThat(latestPriceFromItem).isEqualTo(price2);
  }

  @Test
  void shouldThrowExceptionForNoPriceHistory() {
    // Arrange
    List<Price> priceHistory = new ArrayList<>(List.of());
    Item item = new Item("Item 1", priceHistory);

    // Act & Assert
    assertThatException().isThrownBy(item::getLatestPrice).isInstanceOf(PriceHistoryException.class);
  }

  @Test
  void shouldReturnAveragedPriceByDay() {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(1.57, 1.64, LocalDateTime.parse("2023-03-22T21:19:29.522")),
                new Price(1.72, 1.65, LocalDateTime.parse("2023-03-23T20:53:55.155")),
                new Price(1.70, 1.65, LocalDateTime.parse("2023-03-23T21:00:00.293")),
                new Price(2.22, 2.23, LocalDateTime.parse("2023-03-24T22:40:21.711")),
                new Price(2.27, 2.25, LocalDateTime.parse("2023-03-25T00:00:00.595")),
                new Price(2.27, 2.31, LocalDateTime.parse("2023-03-25T15:50:54.844")),
                new Price(2.21, 2.44, LocalDateTime.parse("2023-03-25T18:00:00.348")),
                new Price(2.25, 2.35, LocalDateTime.parse("2023-03-26T00:00:00.540")),
                new Price(2.21, 2.16, LocalDateTime.parse("2023-03-26T13:39:46.159")),
                new Price(2.24, 2.29, LocalDateTime.parse("2023-03-26T21:00:00.424")),
                new Price(2.07, 2.12, LocalDateTime.parse("2023-03-28T19:02:27.136")),
                new Price(2.08, 2.15, LocalDateTime.parse("2023-03-28T21:00:00.390")),
                new Price(2.14, 2.21, LocalDateTime.parse("2023-03-29T20:59:36.188")),
                new Price(2.14, 2.21, LocalDateTime.parse("2023-03-29T21:00:00.407")),
                new Price(2.10, 2.11, LocalDateTime.parse("2023-03-30T18:42:29.344")),
                new Price(2.59, 2.51, LocalDateTime.parse("2023-03-31T20:36:07.414")),
                new Price(1.69, 2.77, LocalDateTime.parse("2023-04-01T14:36:04.615")),
                new Price(2.67, 2.77, LocalDateTime.parse("2023-04-01T15:00:00.313")),
                new Price(2.77, 2.79, LocalDateTime.parse("2023-04-01T19:24:46.507")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    List<Price> pricesByDay = item.calculateAverageAndMedianPricesPerDay();

    // Assert
    assertThat(pricesByDay).hasSize(11);
    List<Price> expectedPrices =
        List.of(
            new Price(1.57, 1.64, LocalDateTime.parse("2023-03-22T00:00")),
            new Price(1.71, 1.65, LocalDateTime.parse("2023-03-23T00:00")),
            new Price(2.22, 2.23, LocalDateTime.parse("2023-03-24T00:00")),
            new Price(2.25, 2.33, LocalDateTime.parse("2023-03-25T00:00")),
            new Price(2.23, 2.27, LocalDateTime.parse("2023-03-26T00:00")),
            new Price(2.08, 2.14, LocalDateTime.parse("2023-03-28T00:00")),
            new Price(2.14, 2.21, LocalDateTime.parse("2023-03-29T00:00")),
            new Price(2.10, 2.11, LocalDateTime.parse("2023-03-30T00:00")),
            new Price(2.59, 2.51, LocalDateTime.parse("2023-03-31T00:00")),
            new Price(2.38, 2.78, LocalDateTime.parse("2023-04-01T00:00")),
            new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T00:00")));

    assertThat(pricesByDay).containsExactlyInAnyOrderElementsOf(expectedPrices);
  }

  @Test
  void shouldIgnore0ValuesForAverages() {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(0.00, 1.64, LocalDateTime.parse("2023-03-22T21:19:29.522")),
                new Price(1.72, 0.00, LocalDateTime.parse("2023-03-23T20:53:55.155")),
                new Price(1.70, 0.00, LocalDateTime.parse("2023-03-23T21:00:00.293")),
                new Price(2.22, 0.00, LocalDateTime.parse("2023-03-24T22:40:21.711")),
                new Price(2.27, 2.25, LocalDateTime.parse("2023-03-25T00:00:00.595")),
                new Price(2.27, 0.00, LocalDateTime.parse("2023-03-25T15:50:54.844")),
                new Price(2.21, 2.44, LocalDateTime.parse("2023-03-25T18:00:00.348")),
                new Price(2.25, 2.35, LocalDateTime.parse("2023-03-26T00:00:00.540")),
                new Price(0.00, 2.16, LocalDateTime.parse("2023-03-26T13:39:46.159")),
                new Price(2.24, 2.29, LocalDateTime.parse("2023-03-26T21:00:00.424")),
                new Price(0.00, 2.12, LocalDateTime.parse("2023-03-28T19:02:27.136")),
                new Price(0.00, 2.15, LocalDateTime.parse("2023-03-28T21:00:00.390"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    List<Price> pricesByDay = item.calculateAverageAndMedianPricesPerDay();

    // Assert
    assertThat(pricesByDay).hasSize(6);
    List<Price> expectedPrices =
        List.of(
            new Price(0.00, 1.64, LocalDateTime.parse("2023-03-22T00:00")),
            new Price(1.71, 0.00, LocalDateTime.parse("2023-03-23T00:00")),
            new Price(2.22, 0.00, LocalDateTime.parse("2023-03-24T00:00")),
            new Price(2.25, 2.34, LocalDateTime.parse("2023-03-25T00:00")),
            new Price(2.25, 2.27, LocalDateTime.parse("2023-03-26T00:00")),
            new Price(0.00, 2.14, LocalDateTime.parse("2023-03-28T00:00")));
    assertThat(pricesByDay).containsExactlyInAnyOrderElementsOf(expectedPrices);
  }

  @Test
  void shouldCalculatePriceTrend() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(1.69, 2.77, LocalDateTime.parse("2023-04-01T14:36:04.615")),
                new Price(2.67, 2.77, LocalDateTime.parse("2023-04-01T15:00:00.313")),
                new Price(2.77, 2.79, LocalDateTime.parse("2023-04-01T19:24:46.507")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    PriceTrend priceTrend = item.calculatePriceTrendByDay();

    // Assert
    PriceTrend expectedPriceTrend = new PriceTrend(0.18, 0.0756, -0.16, -0.0576);
    assertThat(priceTrend).isEqualTo(expectedPriceTrend);
  }

  @Test
  void shouldCalculatePriceTrendWithDayMissing() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(1.69, 2.77, LocalDateTime.parse("2023-03-31T14:36:04.615")),
                new Price(2.67, 2.77, LocalDateTime.parse("2023-03-31T15:00:00.313")),
                new Price(2.77, 2.79, LocalDateTime.parse("2023-03-31T19:24:46.507")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    PriceTrend priceTrend = item.calculatePriceTrendByDay();

    // Assert
    PriceTrend expectedPriceTrend = new PriceTrend(0.18, 0.0756, -0.16, -0.0576);
    assertThat(priceTrend).isEqualTo(expectedPriceTrend);
  }

  @Test
  void shouldCalculatePriceTrendWithOnlyOneDay() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-04-02T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    PriceTrend priceTrend = item.calculatePriceTrendByDay();

    // Assert
    PriceTrend expectedPriceTrend = new PriceTrend(0.00, 0.0000, 0.00, 0.0000);
    assertThat(priceTrend).isEqualTo(expectedPriceTrend);
  }

  @Test
  void shouldShouldThrowPriceExceptionIfHistoryIsEmtpy() {
    // Arrange
    List<Price> priceHistory = new ArrayList<>(List.of());
    Item item = new Item("Item 1", priceHistory);

    // Act & Assert
    assertThatException().isThrownBy(item::calculatePriceTrendByDay).isInstanceOf(PriceHistoryException.class);
  }

  @Test
  void shouldCalculate7DayPriceTrend() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(1.69, 2.77, LocalDateTime.parse("2023-03-02T14:36:04.615")),
                new Price(2.67, 2.77, LocalDateTime.parse("2023-03-02T15:00:00.313")),
                new Price(2.77, 2.79, LocalDateTime.parse("2023-03-02T19:24:46.507")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    PriceTrend priceTrend7Days = item.calculatePriceTrendByDay(7, ChronoUnit.DAYS);

    // Assert
    PriceTrend expectedPriceTrend = new PriceTrend(0.18, 0.0756, -0.16, -0.0576);
    assertThat(priceTrend7Days).isEqualTo(expectedPriceTrend);
  }

  @Test
  void shouldCalculate7DayPriceTrendWithSmallerGap() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(1.69, 2.77, LocalDateTime.parse("2023-03-05T14:36:04.615")),
                new Price(2.67, 2.77, LocalDateTime.parse("2023-03-05T15:00:00.313")),
                new Price(2.77, 2.79, LocalDateTime.parse("2023-03-05T19:24:46.507")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    PriceTrend priceTrend7Days = item.calculatePriceTrendByDay(7, ChronoUnit.DAYS);

    // Assert
    PriceTrend expectedPriceTrend = new PriceTrend(0.18, 0.0756, -0.16, -0.0576);
    assertThat(priceTrend7Days).isEqualTo(expectedPriceTrend);
  }

  @Test
  void shouldCalculate7DayPriceTrendWithIntermediateValues() throws PriceHistoryException {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(1.69, 2.77, LocalDateTime.parse("2023-03-02T14:36:04.615")),
                new Price(2.67, 2.77, LocalDateTime.parse("2023-03-02T15:00:00.313")),
                new Price(2.77, 2.79, LocalDateTime.parse("2023-03-02T19:24:46.507")),
                new Price(3.00, 2.79, LocalDateTime.parse("2023-03-03T19:24:46.507")),
                new Price(3.01, 2.79, LocalDateTime.parse("2023-03-04T19:24:46.507")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act
    PriceTrend priceTrend7Days = item.calculatePriceTrendByDay(7, ChronoUnit.DAYS);

    // Assert
    PriceTrend expectedPriceTrend = new PriceTrend(0.18, 0.0756, -0.16, -0.0576);
    assertThat(priceTrend7Days).isEqualTo(expectedPriceTrend);
  }

  @Test
  void shouldThrowExceptionForLargerGap() {
    // Arrange
    List<Price> priceHistory =
        new ArrayList<>(
            List.of(
                new Price(1.69, 2.77, LocalDateTime.parse("2023-03-01T14:36:04.615")),
                new Price(2.67, 2.77, LocalDateTime.parse("2023-03-01T15:00:00.313")),
                new Price(2.77, 2.79, LocalDateTime.parse("2023-03-01T19:24:46.507")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T11:45:32.497")),
                new Price(2.56, 2.62, LocalDateTime.parse("2023-03-09T12:00:00.263"))));
    Item item = new Item("Item 1", priceHistory);

    // Act && Assert
    assertThatException().isThrownBy(() -> item.calculatePriceTrendByDay(7, ChronoUnit.DAYS))
        .isInstanceOf(PriceHistoryException.class).withMessage("There is no Price within 7 Days prior to the latest Price.");
  }
}
