package com.hinderegger.steaminventorytracker.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PriceTrendTest {

  @Test
  void shouldCreatePriceTrendWithConstructor() {
    // Arrange & Act
    PriceTrend trend = new PriceTrend(10.5, 0.25, 11.5, 0.3);
    
    // Assert
    assertThat(trend.absolutePriceChange()).isEqualTo(10.5);
    assertThat(trend.percentagePriceChange()).isEqualTo(0.25);
    // Note: There appears to be a naming mismatch in how these fields are used in the codebase
    // percentageMedianChange is actually used for absoluteMedianChange
    // absoluteMedianChange is actually used for percentageMedianChange
    assertThat(trend.percentageMedianChange()).isEqualTo(11.5);
    assertThat(trend.absoluteMedianChange()).isEqualTo(0.3);
  }
  
  @Test
  void shouldImplementEqualsAndHashCode() {
    // Arrange
    PriceTrend trend1 = new PriceTrend(10.5, 0.25, 11.5, 0.3);
    PriceTrend trend2 = new PriceTrend(10.5, 0.25, 11.5, 0.3);
    PriceTrend trend3 = new PriceTrend(20.0, 0.5, 21.0, 0.6);
    
    // Assert
    assertThat(trend1).isEqualTo(trend2);
    assertThat(trend1).isNotEqualTo(trend3);
    assertThat(trend1.hashCode()).isEqualTo(trend2.hashCode());
    assertThat(trend1.hashCode()).isNotEqualTo(trend3.hashCode());
  }
  
  @Test
  void shouldImplementToString() {
    // Arrange
    PriceTrend trend = new PriceTrend(10.5, 0.25, 11.5, 0.3);
    
    // Act
    String toString = trend.toString();
    
    // Assert
    assertThat(toString).contains("absolutePriceChange=10.5");
    assertThat(toString).contains("percentagePriceChange=0.25");
    assertThat(toString).contains("percentageMedianChange=11.5");
    assertThat(toString).contains("absoluteMedianChange=0.3");
  }
  
  @Test
  void shouldHandleNegativeValues() {
    // Arrange
    PriceTrend trend = new PriceTrend(-10.5, -0.25, -11.5, -0.3);
    
    // Assert
    assertThat(trend.absolutePriceChange()).isEqualTo(-10.5);
    assertThat(trend.percentagePriceChange()).isEqualTo(-0.25);
    assertThat(trend.percentageMedianChange()).isEqualTo(-11.5);
    assertThat(trend.absoluteMedianChange()).isEqualTo(-0.3);
  }
  
  @Test
  void shouldHandleZeroValues() {
    // Arrange
    PriceTrend trend = new PriceTrend(0.0, 0.0, 0.0, 0.0);
    
    // Assert
    assertThat(trend.absolutePriceChange()).isEqualTo(0.0);
    assertThat(trend.percentagePriceChange()).isEqualTo(0.0);
    assertThat(trend.percentageMedianChange()).isEqualTo(0.0);
    assertThat(trend.absoluteMedianChange()).isEqualTo(0.0);
  }
}