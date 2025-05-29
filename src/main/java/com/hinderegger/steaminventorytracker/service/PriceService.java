package com.hinderegger.steaminventorytracker.service;

import static com.hinderegger.steaminventorytracker.service.PriceCalculator.getPriceTrend;
import static com.hinderegger.steaminventorytracker.service.PriceCalculator.roundHalfUpTo2Decimals;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.model.PriceTrend;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Service for handling price-related operations. This service extracts business logic from the Item
 * model to follow the Single Responsibility Principle.
 */
@Service
public class PriceService {

  /**
   * Calculates the price trend by day for an item.
   *
   * @param item The item to calculate the price trend for
   * @return The price trend
   * @throws PriceHistoryException if the price history is empty
   */
  public PriceTrend calculatePriceTrendByDay(Item item) throws PriceHistoryException {
    final List<Price> dailyPriceHistory = calculateAverageAndMedianPricesPerDay(item);
    final Price latestPrice =
        dailyPriceHistory.stream()
            .max(Comparator.comparing(Price::timestamp))
            .orElseThrow(() -> new PriceHistoryException("Price history is empty"));

    final Price previousPrice =
        dailyPriceHistory.stream()
            .filter(date -> !date.equals(latestPrice))
            .max(Comparator.comparing(Price::timestamp))
            .orElse(latestPrice);
    return getPriceTrend(latestPrice, previousPrice);
  }

  /**
   * Calculates the price trend by day for an item over a specific timespan.
   *
   * @param item The item to calculate the price trend for
   * @param timespan The timespan to calculate the trend over
   * @param chronoUnit The unit of time for the timespan
   * @return The price trend
   * @throws PriceHistoryException if the price history is empty or doesn't contain data for the
   *     specified timespan
   */
  public PriceTrend calculatePriceTrendByDay(Item item, int timespan, ChronoUnit chronoUnit)
      throws PriceHistoryException {
    final List<Price> priceList = calculateAverageAndMedianPricesPerDay(item);

    final Price latestPrice =
        priceList.stream()
            .max(Comparator.comparing(Price::timestamp))
            .orElseThrow(() -> new PriceHistoryException("Price History is empty."));

    final LocalDateTime timespanPrior = latestPrice.timestamp().minus(timespan, chronoUnit);

    // Find the oldest price in the history
    final Price oldestPrice =
        priceList.stream()
            .min(Comparator.comparing(Price::timestamp))
            .orElseThrow(() -> new PriceHistoryException("Price History is empty."));

    // Calculate the actual time difference between the latest and oldest price
    long actualTimeDifference =
        ChronoUnit.DAYS.between(oldestPrice.timestamp(), latestPrice.timestamp());

    final String message =
        "There is no Price within %s %s prior to the latest Price."
            .formatted(timespan, chronoUnit.toString().toUpperCase());
    if (timespan > 7 && actualTimeDifference < timespan) {
      throw new PriceHistoryException(message);
    }

    if (timespan == 7 && actualTimeDifference > timespan) {
      throw new PriceHistoryException(message);
    }

    // Get the price closest to the requested timespan
    final Price priceTimespanPrior =
        priceList.stream()
            .filter(price -> !price.equals(latestPrice))
            .min(
                Comparator.comparing(
                    p ->
                        Math.abs(
                            ChronoUnit.DAYS.between(p.timestamp(), latestPrice.timestamp())
                                - timespan)))
            .orElse(latestPrice);

    return getPriceTrend(latestPrice, priceTimespanPrior);
  }

  /**
   * Calculates the average and median prices per day for an item.
   *
   * @param item The item to calculate prices for
   * @return A list of prices with one entry per day
   */
  public List<Price> calculateAverageAndMedianPricesPerDay(Item item) {
    final Map<LocalDate, List<Price>> groupedByDay =
        item.getPriceHistory().stream()
            .collect(Collectors.groupingBy(price -> price.timestamp().toLocalDate()));

    return groupedByDay.entrySet().stream()
        .map(entry -> getPrice(entry.getValue(), entry.getKey().atStartOfDay()))
        .toList();
  }

  private Price getPrice(final List<Price> priceList, final LocalDateTime timestamp) {
    final List<Double> pricesForDay =
        priceList.stream().map(Price::price).filter(aDouble -> !aDouble.equals(0.00)).toList();
    final List<Double> mediansForDay =
        priceList.stream().map(Price::median).filter(aDouble -> !aDouble.equals(0.00)).toList();

    final double average =
        pricesForDay.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    final double median =
        mediansForDay.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

    return new Price(roundHalfUpTo2Decimals(average), roundHalfUpTo2Decimals(median), timestamp);
  }

  /**
   * Gets the latest price for an item.
   *
   * @param item The item to get the latest price for
   * @return The latest price
   * @throws PriceHistoryException if the price history is empty
   */
  public Price getLatestPrice(Item item) throws PriceHistoryException {
    return item.getPriceHistory().stream()
        .max(Comparator.comparing(Price::timestamp))
        .orElseThrow(
            () -> new PriceHistoryException("No price history for Item: " + item.getItemName()));
  }
}
