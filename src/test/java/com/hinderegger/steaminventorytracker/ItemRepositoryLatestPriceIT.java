package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hinderegger.steaminventorytracker.persistence.ItemEntity;
import com.hinderegger.steaminventorytracker.persistence.PriceEntity;
import com.hinderegger.steaminventorytracker.repository.jpa.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresTestContainerConfig.class)
class ItemRepositoryLatestPriceIT {

  @Autowired
  ItemRepository itemRepository;

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

    // Persist via JPA entities
    var akE = new ItemEntity(ak.getItemName());
    akE.getPriceHistory().add(new PriceEntity(akE, 5.00, 5.10, t1));
    akE.getPriceHistory().add(new PriceEntity(akE, 5.50, 5.60, t2));
    akE.getPriceHistory().add(new PriceEntity(akE, 5.58, 6.10, t3));

    var m4E = new ItemEntity(m4.getItemName());
    m4E.getPriceHistory().add(new PriceEntity(m4E, 9.99, 10.10, s1));
    m4E.getPriceHistory().add(new PriceEntity(m4E, 10.49, 10.20, s2));

    itemRepository.saveAll(java.util.List.of(akE, m4E));

    // Act
    List<ItemRepository.ItemWithLatestPriceDTO> projected = itemRepository.findAllWithLatestPrice();

    // Assert
    assertThat(projected).hasSize(2);

    Map<String, ItemRepository.ItemWithLatestPriceDTO> byName =
        projected.stream().collect(Collectors.toMap(ItemRepository.ItemWithLatestPriceDTO::itemName, Function.identity()));

    // AK item assertions
    ItemRepository.ItemWithLatestPriceDTO akResult = byName.get("AK-47 | Redline");
    assertThat(akResult).isNotNull();

    assertThat(akResult.price()).isEqualTo(5.58);
    assertThat(akResult.median()).isEqualTo(6.10);
    assertThat(akResult.timestamp()).isEqualTo(t3);

    // M4 item assertions
    ItemRepository.ItemWithLatestPriceDTO m4Result = byName.get("M4A1-S | Guardian");
    assertThat(m4Result).isNotNull();

    assertThat(m4Result.price()).isEqualTo(10.49);
    assertThat(m4Result.median()).isEqualTo(10.20);
    assertThat(m4Result.timestamp()).isEqualTo(s2);
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

    // Persist single item via JPA entities
    var dE = new ItemEntity(deagle.getItemName());
    dE.getPriceHistory().add(new PriceEntity(dE, 150.00, 140.00, h1));
    dE.getPriceHistory().add(new PriceEntity(dE, 180.00, 170.00, h2));
    dE.getPriceHistory().add(new PriceEntity(dE, 200.00, 190.00, h3));
    itemRepository.save(dE);

    // Act
    List<ItemRepository.ItemWithLatestPriceDTO> projected = itemRepository.findAllWithLatestPrice();

    // Assert
    assertThat(projected).hasSize(1);
    ItemRepository.ItemWithLatestPriceDTO result = projected.getFirst();
    assertThat(result.itemName()).isEqualTo("Desert Eagle | Blaze");
    assertThat(result.price()).isEqualTo(200.00);
    assertThat(result.median()).isEqualTo(190.00);
    assertThat(result.timestamp()).isEqualTo(h3);
  }
}
