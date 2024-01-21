package com.hinderegger.steaminventorytracker.model;

import static com.hinderegger.steaminventorytracker.service.PriceCalculator.*;

import com.hinderegger.steaminventorytracker.service.PriceHistoryException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Item {
  @Id @Indexed private final String itemName;
  private final List<Price> priceHistory;

  public PriceTrend calculatePriceTrendByDay() throws PriceHistoryException {
    final List<Price> dailyPriceHistory = this.calculateAverageAndMedianPricesPerDay();
    final Price latestPrice =
        dailyPriceHistory.stream()
            .max(Comparator.comparing(Price::getTimestamp))
            .orElseThrow(() -> new PriceHistoryException("Price history is empty"));

    final Price previousPrice =
        dailyPriceHistory.stream()
            .filter(date -> !date.equals(latestPrice))
            .max(Comparator.comparing(Price::getTimestamp))
            .orElse(latestPrice);
    return getPriceTrend(latestPrice, previousPrice);
  }

  public void addPrice(final Price price) {
    priceHistory.add(price);
  }

  public Price getLatestPrice() throws PriceHistoryException {
    return this.priceHistory.stream()
        .max(Comparator.comparing(Price::getTimestamp))
        .orElseThrow(
            () -> new PriceHistoryException("No price history for Item: " + this.getItemName()));
  }

  public List<Price> calculateAverageAndMedianPricesPerDay() {
    final Map<LocalDate, List<Price>> groupedByDay =
        this.priceHistory.stream()
            .collect(Collectors.groupingBy(price -> price.getTimestamp().toLocalDate()));

    return groupedByDay.entrySet().stream()
        .map(
            entry -> {
              final List<Double> pricesForDay =
                  entry.getValue().stream()
                      .map(Price::getPrice)
                      .filter(aDouble -> !aDouble.equals(0.00))
                      .toList();
              final List<Double> mediansForDay =
                  entry.getValue().stream()
                      .map(Price::getMedian)
                      .filter(aDouble -> !aDouble.equals(0.00))
                      .toList();

              final double average =
                  pricesForDay.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
              final double median =
                  mediansForDay.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

              return new Price(
                  roundHalfUpTo2Decimals(average),
                  roundHalfUpTo2Decimals(median),
                  entry.getKey().atStartOfDay());
            })
        .toList();
  }

  public PriceTrend calculatePriceTrendByDay(final int timespan, final ChronoUnit chronoUnit)
      throws PriceHistoryException {
    final List<Price> priceList = this.calculateAverageAndMedianPricesPerDay();

    final Price latestPrice =
        priceList.stream()
            .max(Comparator.comparing(Price::getTimestamp))
            .orElseThrow(() -> new PriceHistoryException("Price History is empty."));

    final LocalDateTime sevenDaysPrior = latestPrice.getTimestamp().minus(timespan, chronoUnit);
    final Price price7DaysPrior =
        priceList.stream()
            .filter(
                price ->
                    price.getTimestamp().isEqual(sevenDaysPrior)
                        || price.getTimestamp().isBefore(latestPrice.getTimestamp())
                            && price.getTimestamp().isAfter(sevenDaysPrior))
            .min(Comparator.comparing(Price::getTimestamp))
            .orElseThrow(
                () ->
                    new PriceHistoryException(
                        "There is no Price within %s %s prior to the latest Price."
                            .formatted(timespan, chronoUnit)));

    return getPriceTrend(latestPrice, price7DaysPrior);
  }
}
