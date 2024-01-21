package com.hinderegger.steaminventorytracker.model;

import static com.hinderegger.steaminventorytracker.PriceCalculator.*;

import com.hinderegger.steaminventorytracker.PriceHistoryException;
import com.hinderegger.steaminventorytracker.PriceTrend;
import java.time.LocalDate;
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
    Price latestPrice =
        dailyPriceHistory.stream()
            .max(Comparator.comparing(Price::getTimestamp))
            .orElseThrow(() -> new PriceHistoryException("Price history is empty"));

    Price previousPrice =
        dailyPriceHistory.stream()
            .filter(date -> !date.equals(latestPrice))
            .max(Comparator.comparing(Price::getTimestamp))
            .orElse(latestPrice);
    double absolutePriceChange =
        getAbsoluteChange(latestPrice.getPrice(), previousPrice.getPrice());
    double percentagePriceChange =
        getPercentageChange(absolutePriceChange, previousPrice.getPrice());
    double absoluteMedianChange =
        getAbsoluteChange(latestPrice.getMedian(), previousPrice.getMedian());
    double percentageMedianChange =
        getPercentageChange(absoluteMedianChange, previousPrice.getMedian());
    return new PriceTrend(
        roundHalfUpTo2Decimals(absolutePriceChange),
        roundHalfUpTo4Decimals(percentagePriceChange),
        roundHalfUpTo2Decimals(absoluteMedianChange),
        roundHalfUpTo4Decimals(percentageMedianChange));
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
    Map<LocalDate, List<Price>> groupedByDay =
        this.priceHistory.stream()
            .collect(Collectors.groupingBy(price -> price.getTimestamp().toLocalDate()));

    return groupedByDay.entrySet().stream()
        .map(
            entry -> {
              List<Double> pricesForDay =
                  entry.getValue().stream()
                      .map(Price::getPrice)
                      .filter(aDouble -> !aDouble.equals(0.00))
                      .toList();
              List<Double> mediansForDay =
                  entry.getValue().stream()
                      .map(Price::getMedian)
                      .filter(aDouble -> !aDouble.equals(0.00))
                      .toList();

              double average =
                  pricesForDay.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
              double median =
                  mediansForDay.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

              return new Price(
                  roundHalfUpTo2Decimals(average),
                  roundHalfUpTo2Decimals(median),
                  entry.getKey().atStartOfDay());
            })
        .toList();
  }
}
