package com.hinderegger.steaminventorytracker.service;

import static org.mockito.Mockito.*;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.service.strategy.AsyncSteamRequestStrategy;
import com.hinderegger.steaminventorytracker.service.strategy.SteamRequestStrategy;
import com.hinderegger.steaminventorytracker.service.strategy.SyncSteamRequestStrategy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SteamInventoryTrackerService. This test class uses mocks for the strategy
 * implementations to test the service's behavior.
 */
class SteamInventoryTrackerServiceTest {

  private SteamInventoryTrackerService service;
  private ItemRepository itemRepository;
  private AsyncSteamRequestStrategy asyncStrategy;
  private SyncSteamRequestStrategy syncStrategy;
  private List<Item> testItems;

  @BeforeEach
  void setUp() {
    // Create mocks
    itemRepository = mock(ItemRepository.class);
    asyncStrategy = mock(AsyncSteamRequestStrategy.class);
    syncStrategy = mock(SyncSteamRequestStrategy.class);

    // Create the service with mocks
    service = new SteamInventoryTrackerService(itemRepository, asyncStrategy, syncStrategy);

    // Set up test data
    Price price = new Price(0.1, 0.11, LocalDateTime.of(2023, 12, 20, 15, 0, 0));
    ArrayList<Price> priceHistory = new ArrayList<>(List.of(price));
    Item item = new Item("Test Item 1", priceHistory);
    testItems = new ArrayList<>(List.of(item, item));

    // Configure mock behavior
    when(itemRepository.findAll()).thenReturn(testItems);
  }

  @Test
  void shouldRequestItemsUsingAsyncStrategy() {
    // Act
    service.requestItems();

    // Assert
    verify(itemRepository).findAll();
    verify(asyncStrategy).requestItems(testItems);
    verifyNoInteractions(syncStrategy);
  }

  @Test
  void shouldRequestItemsUsingSyncStrategy() {
    // Act
    service.requestItemsSync();

    // Assert
    verify(itemRepository).findAll();
    verify(syncStrategy).requestItems(testItems);
    verifyNoInteractions(asyncStrategy);
  }

  @Test
  void shouldRequestItemsUsingCustomStrategy() {
    // Arrange
    SteamRequestStrategy customStrategy = mock(SteamRequestStrategy.class);

    // Act
    service.requestItemsWithStrategy(customStrategy);

    // Assert
    verify(itemRepository).findAll();
    verify(customStrategy).requestItems(testItems);
    verifyNoInteractions(asyncStrategy);
    verifyNoInteractions(syncStrategy);
  }
}
