package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.service.strategy.AsyncSteamRequestStrategy;
import com.hinderegger.steaminventorytracker.service.strategy.SteamRequestStrategy;
import com.hinderegger.steaminventorytracker.service.strategy.SyncSteamRequestStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for tracking Steam inventory items and their prices.
 * This service uses the Strategy pattern to handle different ways of requesting data from Steam.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SteamInventoryTrackerService {
  private final ItemService itemService;
  private final AsyncSteamRequestStrategy asyncStrategy;
  private final SyncSteamRequestStrategy syncStrategy;

  /**
   * Requests item data from Steam asynchronously.
   * This method uses the AsyncSteamRequestStrategy for non-blocking operations.
   */
  public void requestItems() {
    log.info("Starting asynchronous Steam Market request");
    final List<Item> allItems = itemService.getAllItems();
    asyncStrategy.requestItems(allItems);
  }

  /**
   * Requests item data from Steam synchronously.
   * This method uses the SyncSteamRequestStrategy for blocking operations.
   */
  public void requestItemsSync() {
    log.info("Starting synchronous Steam Market request");
    final List<Item> allItems = itemService.getAllItems();
    syncStrategy.requestItems(allItems);
  }

  /**
   * Requests item data from Steam using the specified strategy.
   *
   * @param strategy The strategy to use for requesting data
   */
  public void requestItemsWithStrategy(SteamRequestStrategy strategy) {
    log.info("Starting Steam Market request with custom strategy");
    final List<Item> allItems = itemService.getAllItems();
    strategy.requestItems(allItems);
  }
}
