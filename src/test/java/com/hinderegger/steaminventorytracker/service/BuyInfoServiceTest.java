package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hinderegger.steaminventorytracker.MongoDBTestContainerConfig;
import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.repository.BuyInfoRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@Testcontainers
@ContextConfiguration(classes = MongoDBTestContainerConfig.class)
class BuyInfoServiceTest {
  @Autowired MongoTemplate mongoTemplate;

  @Autowired private BuyInfoRepository buyInfoRepository;
  private BuyInfoService testee;

  @BeforeEach
  void setUp() {
    testee = new BuyInfoService(buyInfoRepository);
  }

  @AfterEach
  void tearDown() {
    buyInfoRepository.deleteAll();
  }

  @Test
  void shouldAddBuyInfos() {
    // Arrange
    List<BuyInfo> buyInfos =
        List.of(new BuyInfo("Test Item", 5, 0.1), new BuyInfo("Other Item", 1, 1));
    // Act
    testee.addBuyInfos(buyInfos);
    // Assert
    List<BuyInfo> allBuyInfos = testee.getAllBuyInfos();
    assertThat(allBuyInfos).hasSize(2);
    assertThat(allBuyInfos.getFirst().getItemName()).isEqualTo("Test Item");
    assertThat(allBuyInfos.getFirst().getAmount()).isEqualTo(5);
    assertThat(allBuyInfos.getFirst().getBuyPrice()).isEqualTo(0.1);
    assertThat(allBuyInfos.get(1).getItemName()).isEqualTo("Other Item");
    assertThat(allBuyInfos.get(1).getAmount()).isEqualTo(1);
    assertThat(allBuyInfos.get(1).getBuyPrice()).isEqualTo(1);
  }

  @Test
  void shouldAddBuyInfo() {
    // Arrange
    // Act
    testee.addBuyInfo("Test Item", 5, 0.1);
    // Assert
    List<BuyInfo> allBuyInfos = testee.getAllBuyInfos();
    assertThat(allBuyInfos).isNotEmpty();
    assertThat(allBuyInfos.getFirst().getItemName()).isEqualTo("Test Item");
    assertThat(allBuyInfos.getFirst().getAmount()).isEqualTo(5);
    assertThat(allBuyInfos.getFirst().getBuyPrice()).isEqualTo(0.1);
  }
}
