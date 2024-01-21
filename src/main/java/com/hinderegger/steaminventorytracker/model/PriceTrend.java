package com.hinderegger.steaminventorytracker.model;

public record PriceTrend(
    double absolutePriceChange,
    double percentagePriceChange,
    double percentageMedianChange,
    double absoluteMedianChange) {}
