package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.model.PriceTrend;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceCalculator {
  private PriceCalculator() {}

  public static double roundHalfUpTo2Decimals(final double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  private static double roundHalfUpTo4Decimals(final double value) {
    return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
  }

  private static double getPercentageChange(
      final double absoluteChange, final double previousPrice) {
    return absoluteChange / previousPrice;
  }

  private static double getAbsoluteChange(final double latestPrice, final double previousPrice) {
    return latestPrice - previousPrice;
  }

  /**
   * Calculates the price trend between two price points.
   *
   * @param latestPrice The most recent price
   * @param previousPrice The previous price to compare against
   * @return A PriceTrend object containing absolute and percentage changes
   */
  public static PriceTrend getPriceTrend(final Price latestPrice, final Price previousPrice) {
    double absolutePriceChange = getAbsoluteChange(latestPrice.price(), previousPrice.price());
    double percentagePriceChange = getPercentageChange(absolutePriceChange, previousPrice.price());
    double absoluteMedianChange = getAbsoluteChange(latestPrice.median(), previousPrice.median());
    double percentageMedianChange =
        getPercentageChange(absoluteMedianChange, previousPrice.median());
    return new PriceTrend(
        roundHalfUpTo2Decimals(absolutePriceChange),
        roundHalfUpTo4Decimals(percentagePriceChange),
        roundHalfUpTo2Decimals(absoluteMedianChange),
        roundHalfUpTo4Decimals(percentageMedianChange));
  }
}
