package com.hinderegger.steaminventorytracker.model;

/**
 * Record representing the trend of price changes.
 * 
 * @param absolutePriceChange The absolute change in price
 * @param percentagePriceChange The percentage change in price
 * @param absoluteMedianChange The absolute change in median price
 * @param percentageMedianChange The percentage change in median price
 */
public record PriceTrend(
    double absolutePriceChange,
    double percentagePriceChange,
    double absoluteMedianChange,
    double percentageMedianChange) {}
