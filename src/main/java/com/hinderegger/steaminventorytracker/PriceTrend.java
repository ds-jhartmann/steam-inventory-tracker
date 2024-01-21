package com.hinderegger.steaminventorytracker;

public record PriceTrend(
    double absolutePriceChange,
    double percentagePriceChange,
    double percentageMedianChange,
    double absoluteMedianChange) {}
