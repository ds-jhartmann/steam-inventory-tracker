package com.hinderegger.steaminventorytracker.model;

public record PriceTrend(
    double absolutePriceChange,
    double percentagePriceChange,
    double absoluteMedianChange,
    double percentageMedianChange) {}
