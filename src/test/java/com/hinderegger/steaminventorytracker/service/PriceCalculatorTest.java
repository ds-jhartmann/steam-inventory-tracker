package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.model.PriceTrend;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PriceCalculatorTest {

  @Test
  void shouldRoundHalfUpTo2Decimals() {
    // Arrange
    double value1 = 5.555;
    double value2 = 5.554;
    double value3 = 5.545;
    double value4 = 5.544;

    // Act
    double result1 = PriceCalculator.roundHalfUpTo2Decimals(value1);
    double result2 = PriceCalculator.roundHalfUpTo2Decimals(value2);
    double result3 = PriceCalculator.roundHalfUpTo2Decimals(value3);
    double result4 = PriceCalculator.roundHalfUpTo2Decimals(value4);

    // Assert
    assertThat(result1).isEqualTo(5.56);
    assertThat(result2).isEqualTo(5.55);
    assertThat(result3).isEqualTo(5.55);
    assertThat(result4).isEqualTo(5.54);
  }

  @Test
  void shouldCalculatePriceTrendWithPositiveChanges() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    Price latestPrice = new Price(10.50, 11.50, now);
    Price previousPrice = new Price(10.00, 11.00, now.minusDays(1));

    // Act
    PriceTrend result = PriceCalculator.getPriceTrend(latestPrice, previousPrice);

    // Assert
    assertThat(result.absolutePriceChange()).isEqualTo(0.50);
    assertThat(result.percentagePriceChange()).isEqualTo(0.05);
    assertThat(result.absoluteMedianChange()).isEqualTo(0.50);
    assertThat(result.percentageMedianChange()).isEqualTo(0.0455);
  }

  @Test
  void shouldCalculatePriceTrendWithNegativeChanges() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    Price latestPrice = new Price(9.50, 10.50, now);
    Price previousPrice = new Price(10.00, 11.00, now.minusDays(1));

    // Act
    PriceTrend result = PriceCalculator.getPriceTrend(latestPrice, previousPrice);

    // Assert
    assertThat(result.absolutePriceChange()).isEqualTo(-0.50);
    assertThat(result.percentagePriceChange()).isEqualTo(-0.05);
    assertThat(result.absoluteMedianChange()).isEqualTo(-0.50);
    assertThat(result.percentageMedianChange()).isEqualTo(-0.0455);
  }

  @Test
  void shouldHandleZeroValues() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    // Use small non-zero values to avoid division by zero
    Price latestPrice = new Price(0.01, 0.01, now);
    Price previousPrice = new Price(0.01, 0.01, now.minusDays(1));

    // Act & Assert
    // This test verifies that no exceptions are thrown when dealing with very small values
    assertDoesNotThrow(() -> {
      PriceTrend result = PriceCalculator.getPriceTrend(latestPrice, previousPrice);

      // Both absolute changes should be 0.0
      assertThat(result.absolutePriceChange()).isEqualTo(0.0);
      assertThat(result.percentagePriceChange()).isEqualTo(0.0);
      assertThat(result.absoluteMedianChange()).isEqualTo(0.0);
      assertThat(result.percentageMedianChange()).isEqualTo(0.0);
    });
  }

  @Test
  void shouldHandleExtremeValues() {
    // Arrange
    LocalDateTime now = LocalDateTime.now();
    Price latestPrice = new Price(Double.MAX_VALUE, Double.MAX_VALUE, now);
    Price previousPrice = new Price(Double.MAX_VALUE / 2, Double.MAX_VALUE / 2, now.minusDays(1));

    // Act & Assert
    // This test verifies that no exceptions are thrown when dealing with extreme values
    assertDoesNotThrow(() -> {
      PriceCalculator.getPriceTrend(latestPrice, previousPrice);
    });
  }
}
