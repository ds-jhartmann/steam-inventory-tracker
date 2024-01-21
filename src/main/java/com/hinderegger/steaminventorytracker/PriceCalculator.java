package com.hinderegger.steaminventorytracker;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceCalculator {
  private PriceCalculator() {}

  public static double roundHalfUpTo2Decimals(double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public static double roundHalfUpTo4Decimals(double value) {
    return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
  }

  public static double getPercentageChange(double absoluteChange, double previousPrice) {
    return absoluteChange / previousPrice;
  }

  public static double getAbsoluteChange(double latestPrice, double previousPrice) {
    return latestPrice - previousPrice;
  }
}
