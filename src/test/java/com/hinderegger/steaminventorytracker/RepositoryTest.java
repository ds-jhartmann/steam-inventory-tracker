package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.service.SteamInventoryTrackerService;
import com.hinderegger.steaminventorytracker.service.strategy.AsyncSteamRequestStrategy;
import com.hinderegger.steaminventorytracker.service.strategy.SteamRequestStrategy;
import com.hinderegger.steaminventorytracker.service.strategy.SyncSteamRequestStrategy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for the SteamInventoryTrackerService with MongoDB. This test uses the Strategy
 * pattern to test both synchronous and asynchronous item requests.
 */
@DataMongoTest
@Testcontainers
@ContextConfiguration(classes = MongoDBTestContainerConfig.class)
class RepositoryTest {

  @Autowired MongoTemplate mongoTemplate;
  @Autowired private ItemRepository itemRepository;

  private SteamInventoryTrackerService testee;
  private AsyncSteamRequestStrategy asyncStrategy;
  private SyncSteamRequestStrategy syncStrategy;

  @BeforeEach
  void setUp() {
    // Create mock strategies
    asyncStrategy = mock(AsyncSteamRequestStrategy.class);
    syncStrategy = mock(SyncSteamRequestStrategy.class);

    // Configure the strategies to update items when requestItems is called
    doAnswer(
            invocation -> {
              List<Item> items = invocation.getArgument(0);
              // Process each item - in a real scenario, this would call the Steam API
              for (Item item : items) {
                // Add a test price to each item
                if (item.getItemName().equals("Test 1")) {
                  item.addPrice(new Price(5.79, 5.61, LocalDateTime.now()));
                } else if (item.getItemName().equals("Test 2")) {
                  item.addPrice(new Price(0.99, 0.65, LocalDateTime.now()));
                }
                // Save the updated item
                itemRepository.save(item);
              }
              return null;
            })
        .when(asyncStrategy)
        .requestItems(any());

    // Configure the sync strategy similarly
    doAnswer(
            invocation -> {
              List<Item> items = invocation.getArgument(0);
              for (Item item : items) {
                if (item.getItemName().equals("Test 1")) {
                  item.addPrice(new Price(5.79, 5.61, LocalDateTime.now()));
                } else if (item.getItemName().equals("Test 2")) {
                  item.addPrice(new Price(0.99, 0.65, LocalDateTime.now()));
                }
                itemRepository.save(item);
              }
              return null;
            })
        .when(syncStrategy)
        .requestItems(any());

    // Create the service with mock strategies
    testee = new SteamInventoryTrackerService(itemRepository, asyncStrategy, syncStrategy);
  }

  @AfterEach
  void tearDown() {
    // Delete only the specific items used in tests
    itemRepository.deleteById("Test 1");
    itemRepository.deleteById("Test 2");
  }

  @Test
  void shouldRequestItemAsync() {
    // Arrange
    mongoTemplate.insert(new Item("Test 1", List.of()));

    // Act
    testee.requestItems();

    // Assert
    List<Item> items = itemRepository.findAll();
    assertThat(items).hasSize(1);
    assertThat(items.getFirst().getItemName()).isEqualTo("Test 1");
    assertThat(items.getFirst().getPriceHistory()).isNotEmpty();
    assertThat(items.getFirst().getPriceHistory().getFirst().price()).isEqualTo(5.79);
    assertThat(items.getFirst().getPriceHistory().getFirst().median()).isEqualTo(5.61);
    verify(asyncStrategy, times(1)).requestItems(any());
    verifyNoInteractions(syncStrategy);
  }

  @Test
  void shouldRequestAllItemsAsync() {
    // Arrange
    Item item1 = new Item("Test 1", List.of());
    Item item2 = new Item("Test 2", List.of());
    mongoTemplate.insert(item1);
    mongoTemplate.insert(item2);

    // Act
    testee.requestItems();

    // Assert
    List<Item> items = itemRepository.findAll();
    assertThat(items).hasSize(2);
    Optional<Item> storedItem1 = itemRepository.findById("Test 1");
    Optional<Item> storedItem2 = itemRepository.findById("Test 2");
    assertThat(storedItem1).isPresent();
    assertThat(storedItem2).isPresent();

    // Verify the correct strategy was used
    verify(asyncStrategy, times(1)).requestItems(any());
    verifyNoInteractions(syncStrategy);
  }

  @Test
  void shouldRequestItemSync() {
    // Arrange
    Item item1 = new Item("Test 1", List.of());
    Item item2 = new Item("Test 2", List.of());
    mongoTemplate.insert(item1);
    mongoTemplate.insert(item2);

    // Act
    testee.requestItemsSync();

    // Assert
    List<Item> items = itemRepository.findAll();
    assertThat(items).hasSize(2);
    Optional<Item> storedItem1 = itemRepository.findById("Test 1");
    Optional<Item> storedItem2 = itemRepository.findById("Test 2");
    assertThat(storedItem1).isPresent();
    assertThat(storedItem2).isPresent();

    // Verify the correct strategy was used
    verify(syncStrategy, times(1)).requestItems(any());
    verifyNoInteractions(asyncStrategy);
  }

  @Test
  void shouldUseCustomStrategy() {
    // Arrange
    Item item = new Item("Test 1", List.of());
    mongoTemplate.insert(item);

    // Create a custom strategy
    SteamRequestStrategy customStrategy = mock(SteamRequestStrategy.class);
    doAnswer(
            invocation -> {
              List<Item> items = invocation.getArgument(0);
              for (Item i : items) {
                i.addPrice(new Price(9.99, 9.99, LocalDateTime.now()));
                itemRepository.save(i);
              }
              return null;
            })
        .when(customStrategy)
        .requestItems(any());

    // Act
    testee.requestItemsWithStrategy(customStrategy);

    // Assert
    Item updatedItem = itemRepository.findById("Test 1").orElseThrow();
    assertThat(updatedItem.getPriceHistory()).isNotEmpty();
    assertThat(updatedItem.getPriceHistory().getFirst().price()).isEqualTo(9.99);

    // Verify the correct strategy was used
    verify(customStrategy, times(1)).requestItems(any());
    verifyNoInteractions(asyncStrategy);
    verifyNoInteractions(syncStrategy);
  }
}
