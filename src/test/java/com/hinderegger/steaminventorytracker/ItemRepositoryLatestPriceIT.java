package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@Testcontainers
@ContextConfiguration(classes = MongoDBTestContainerConfig.class)
class ItemRepositoryLatestPriceIT {

  @Autowired MongoTemplate mongoTemplate;
  @Autowired ItemRepository itemRepository;

  @AfterEach
  void cleanup() {
    itemRepository.deleteAll();
  }

  @Test
  void findAllWithLatestPrice_shouldReturnOnlyTheLatestHistoryEntry_perItem() {
    // Arrange: two items with multiple history entries
    LocalDateTime t1 = LocalDateTime.parse("2024-01-01T10:00:00");
    LocalDateTime t2 = LocalDateTime.parse("2024-02-01T10:00:00");
    LocalDateTime t3 = LocalDateTime.parse("2024-03-01T10:00:00");

    Item ak = new Item("AK-47 | Redline", new ArrayList<>());

    ak.addPrice(new Price(5.00, 5.10, t1));
    ak.addPrice(new Price(5.50, 5.60, t2));
    ak.addPrice(new Price(5.58, 6.10, t3)); // latest we expect back

    LocalDateTime s1 = LocalDateTime.parse("2023-11-20T09:00:00");
    LocalDateTime s2 = LocalDateTime.parse("2023-12-20T09:00:00");

    Item m4 = new Item("M4A1-S | Guardian", new ArrayList<>());

    m4.addPrice(new Price(9.99, 10.10, s1));
    m4.addPrice(new Price(10.49, 10.20, s2)); // latest we expect back

    // Insert documents with full histories
    mongoTemplate.insertAll(List.of(ak, m4));

    // Act
    List<Item> projected = itemRepository.findAllWithLatestPrice();

    // Assert
    assertThat(projected).hasSize(2);

    Map<String, Item> byName =
        projected.stream().collect(Collectors.toMap(Item::getItemName, Function.identity()));

    // AK item assertions
    Item akResult = byName.get("AK-47 | Redline");
    assertThat(akResult).isNotNull();
    assertThat(akResult.getPriceHistory()).hasSize(1);
    Price akLatest = akResult.getPriceHistory().getFirst();
    assertThat(akLatest.price()).isEqualTo(5.58);
    assertThat(akLatest.median()).isEqualTo(6.10);
    assertThat(akLatest.timestamp()).isEqualTo(t3);

    // M4 item assertions
    Item m4Result = byName.get("M4A1-S | Guardian");
    assertThat(m4Result).isNotNull();
    assertThat(m4Result.getPriceHistory()).hasSize(1);
    Price m4Latest = m4Result.getPriceHistory().getFirst();
    assertThat(m4Latest.price()).isEqualTo(10.49);
    assertThat(m4Latest.median()).isEqualTo(10.20);
    assertThat(m4Latest.timestamp()).isEqualTo(s2);
  }

  @Test
  void findAllWithLatestPrice_shouldWorkWithSingleItemAndThreeHistoryEntries() {
    // Arrange: one item with 3 entries
    LocalDateTime h1 = LocalDateTime.parse("2025-01-01T00:00:00");
    LocalDateTime h2 = LocalDateTime.parse("2025-02-01T00:00:00");
    LocalDateTime h3 = LocalDateTime.parse("2025-03-01T00:00:00");

    Item deagle = new Item("Desert Eagle | Blaze", new ArrayList<>());

    deagle.addPrice(new Price(150.00, 140.00, h1));
    deagle.addPrice(new Price(180.00, 170.00, h2));
    deagle.addPrice(new Price(200.00, 190.00, h3)); // expected latest

    mongoTemplate.insert(deagle);

    // Act
    List<Item> projected = itemRepository.findAllWithLatestPrice();

    // Assert
    assertThat(projected).hasSize(1);
    Item result = projected.getFirst();
    assertThat(result.getItemName()).isEqualTo("Desert Eagle | Blaze");
    assertThat(result.getPriceHistory()).hasSize(1);
    Price latest = result.getPriceHistory().getFirst();
    assertThat(latest.price()).isEqualTo(200.00);
    assertThat(latest.median()).isEqualTo(190.00);
    assertThat(latest.timestamp()).isEqualTo(h3);
  }
}
