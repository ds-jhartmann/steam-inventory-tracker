package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.mock;

import com.hinderegger.steaminventorytracker.MongoDBTestContainerConfig;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@Testcontainers
@ContextConfiguration(classes = MongoDBTestContainerConfig.class)
class ItemServiceTest {

  @Autowired private ItemRepository itemRepository;
  private ItemService testee;
  private MockTimeProvider mockTimeProvider;

  @BeforeEach
  void setUp() {
    mockTimeProvider = new MockTimeProvider();
    PriceService priceService = mock(PriceService.class);
    testee = new ItemService(itemRepository, mockTimeProvider, priceService);
  }

  @AfterEach
  void tearDown() {
    itemRepository.deleteAll();
  }

  @Test
  void shouldReturnItemIfExisting() {
    // Arrange
    Item item = new Item("Test Item", List.of());
    testee.addItem(item);

    // Act
    Item storedItem = testee.getItemByName("Test Item");

    // Assert
    assertThat(storedItem.getItemName()).isEqualTo("Test Item");
    assertThat(storedItem.getPriceHistory()).isEmpty();
  }

  @Test
  void shouldThrowExceptionIfNotExisting() {
    // Arrange
    Item item = new Item("Test Item", List.of());
    testee.addItem(item);

    // Act & Assert
    assertThatException()
        .isThrownBy(() -> testee.getItemByName("Other Item"))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void shouldStoreNewItemByName() {
    // Arrange

    // Act
    testee.addItem("Test Item");

    // Assert
    Item storedItem = testee.getItemByName("Test Item");
    assertThat(storedItem.getItemName()).isEqualTo("Test Item");
    assertThat(storedItem.getPriceHistory()).isEmpty();
  }

  @Test
  void shouldStoreNewItem() {
    // Arrange
    Item item = new Item("Test Item", List.of());

    // Act
    testee.addItem(item);

    // Assert
    Item storedItem = testee.getItemByName("Test Item");
    assertThat(storedItem.getItemName()).isEqualTo("Test Item");
    assertThat(storedItem.getPriceHistory()).isEmpty();
  }

  @Test
  void shouldThrowExceptionOnDuplicate() {
    // Arrange
    Item item = new Item("Test Item", List.of());
    // Act
    testee.addItem(item);

    // Assert
    assertThatException()
        .isThrownBy(() -> testee.addItem(item))
        .isInstanceOf(ResponseStatusException.class);
    Item storedItem = testee.getItemByName("Test Item");
    assertThat(storedItem.getItemName()).isEqualTo("Test Item");
    assertThat(storedItem.getPriceHistory()).isEmpty();
  }

  @Test
  void shouldReturnAllItems() {
    // Arrange
    Item item = new Item("Test Item", List.of());
    testee.addItem(item);

    // Act
    List<Item> allItems = testee.getAllItems();

    // Assert
    assertThat(allItems).contains(item);
  }

  @Test
  void shouldReturnAllItemsOnEmptyRepo() {
    // Act
    List<Item> allItems = testee.getAllItems();

    // Assert
    assertThat(allItems).isEmpty();
  }

  @Test
  void shouldUpdatePrice() {
    // Arrange
    Item item = new Item("Test Item", List.of());
    testee.addItem(item);

    // Act
    Item updatedItem = testee.updatePriceForItem("Test Item", 0.1, 0.2);

    // Assert
    assertThat(updatedItem.getItemName()).isEqualTo("Test Item");
    assertThat(updatedItem.getPriceHistory()).hasSize(1);
    assertThat(updatedItem.getPriceHistory().get(0).price()).isEqualTo(0.1);
    assertThat(updatedItem.getPriceHistory().get(0).median()).isEqualTo(0.2);
    assertThat(updatedItem.getPriceHistory().get(0).timestamp())
        .isEqualTo(LocalDateTime.of(2023, 12, 20, 15, 0, 0));
  }

  @Test
  void shouldThrowExceptionIfUpdateItemNotExisting() {
    // Arrange
    Item item = new Item("Test Item", List.of());
    testee.addItem(item);

    // Act & Assert
    assertThatException()
        .isThrownBy(() -> testee.updatePriceForItem("Other Item", 0.1, 0.2))
        .isInstanceOf(ResponseStatusException.class);
  }
}
