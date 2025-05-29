package com.hinderegger.steaminventorytracker.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BuyInfoTest {

  @Test
  void shouldCreateBuyInfoWithConstructor() {
    // Arrange & Act
    BuyInfo buyInfo = new BuyInfo("Test Item", 5, 10.50);
    
    // Assert
    assertThat(buyInfo.getItemName()).isEqualTo("Test Item");
    assertThat(buyInfo.getAmount()).isEqualTo(5);
    assertThat(buyInfo.getBuyPrice()).isEqualTo(10.50);
    assertThat(buyInfo.getId()).isNull(); // ID is not set in constructor
  }
  
  @Test
  void shouldSetAndGetProperties() {
    // Arrange
    BuyInfo buyInfo = new BuyInfo("Test Item", 5, 10.50);
    
    // Act
    buyInfo.setId("test-id");
    buyInfo.setItemName("Updated Item");
    buyInfo.setAmount(10);
    buyInfo.setBuyPrice(20.75);
    
    // Assert
    assertThat(buyInfo.getId()).isEqualTo("test-id");
    assertThat(buyInfo.getItemName()).isEqualTo("Updated Item");
    assertThat(buyInfo.getAmount()).isEqualTo(10);
    assertThat(buyInfo.getBuyPrice()).isEqualTo(20.75);
  }
  
  @Test
  void shouldImplementEqualsAndHashCode() {
    // Arrange
    BuyInfo buyInfo1 = new BuyInfo("Test Item", 5, 10.50);
    buyInfo1.setId("test-id");
    
    BuyInfo buyInfo2 = new BuyInfo("Test Item", 5, 10.50);
    buyInfo2.setId("test-id");
    
    BuyInfo buyInfo3 = new BuyInfo("Different Item", 5, 10.50);
    buyInfo3.setId("different-id");
    
    // Assert
    assertThat(buyInfo1).isEqualTo(buyInfo2);
    assertThat(buyInfo1).isNotEqualTo(buyInfo3);
    assertThat(buyInfo1.hashCode()).isEqualTo(buyInfo2.hashCode());
    assertThat(buyInfo1.hashCode()).isNotEqualTo(buyInfo3.hashCode());
  }
  
  @Test
  void shouldImplementToString() {
    // Arrange
    BuyInfo buyInfo = new BuyInfo("Test Item", 5, 10.50);
    buyInfo.setId("test-id");
    
    // Act
    String toString = buyInfo.toString();
    
    // Assert
    assertThat(toString).contains("id=test-id");
    assertThat(toString).contains("itemName=Test Item");
    assertThat(toString).contains("amount=5");
    assertThat(toString).contains("buyPrice=10.5");
  }
}