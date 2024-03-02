package com.hinderegger.steaminventorytracker.controller;

import static com.hinderegger.steaminventorytracker.SteamInventoryTrackerApplication.API_PATH;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.model.PriceTrend;
import com.hinderegger.steaminventorytracker.service.BuyInfoService;
import com.hinderegger.steaminventorytracker.service.CSVExporter;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.PriceHistoryException;
import com.hinderegger.steaminventorytracker.service.SteamInventoryTrackerService;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = API_PATH)
@AllArgsConstructor
@Slf4j
public class SteamInventoryTrackerController {

  private final SteamInventoryTrackerService steamInventoryTrackerService;
  private final ItemService itemService;
  private final BuyInfoService buyInfoService;

  @PostMapping(path = "/addItem")
  public ResponseEntity<Item> addNewItem(@RequestParam final String name) {
    final Item item = itemService.addItem(name);
    return ResponseEntity.ok(item);
  }

  @PostMapping(path = "/registerBuyInfo")
  public ResponseEntity<BuyInfo> registerBuyInfo(
      @RequestParam final String name,
      @RequestParam final Integer amount,
      @RequestParam final Double buyPrice) {
    log.info(
        "Registering Buyer info for '{}', amount: '{}', buy price: '{}'", name, amount, buyPrice);
    final BuyInfo buyInfo = buyInfoService.addBuyInfo(name, amount, buyPrice);
    return ResponseEntity.ok(buyInfo);
  }

  @PostMapping(path = "/registerBuyInfos")
  public ResponseEntity<List<BuyInfo>> registerBuyInfos(@RequestBody final List<BuyInfo> buyInfos) {
    log.info("Adding {} buy infos", buyInfos.size());
    final List<BuyInfo> addedBuyInfos = buyInfoService.addBuyInfos(buyInfos);
    return ResponseEntity.ok(addedBuyInfos);
  }

  @PostMapping(path = "/addItems", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Item>> addNewItems(@RequestBody final List<Item> items) {
    log.info("Adding '{}' new items.", items.size());
    return ResponseEntity.ok(itemService.addItems(items));
  }

  @PostMapping(path = "/updatePrice")
  public ResponseEntity<Item> updatePrice(
      @RequestParam final String name,
      @RequestParam final double price,
      @RequestParam final double median) {
    log.info("Updating price for Item '{}' with price '{}' and median '{}'", name, price, median);
    final Item item = itemService.updatePriceForItem(name, price, median);
    return ResponseEntity.ok(item);
  }

  @GetMapping(path = "/getItem")
  public ResponseEntity<Item> getItemByName(@RequestParam final String name) {
    log.info("Returning Item for name '{}'", name);
    final Item item = itemService.getItemByName(name);
    return ResponseEntity.ok(item);
  }

  @GetMapping(path = "/all")
  public ResponseEntity<List<Item>> getAllItems() {
    log.info("Returning all Items.");
    final List<Item> allItems = itemService.getAllItems();
    return ResponseEntity.ok(allItems);
  }

  @GetMapping(path = "/allNames")
  public ResponseEntity<List<String>> getAllItemNames() {
    log.info("Returning all Items.");
    final List<Item> allItems = itemService.getAllItems();
    return ResponseEntity.ok(allItems.stream().map(Item::getItemName).toList());
  }

  @GetMapping(path = "/startSteamMarketRequest")
  public ResponseEntity<String> startSteamMarketQuery() {
    log.info("Starting Steam Market Request.");
    CompletableFuture.runAsync(steamInventoryTrackerService::requestItemsSync);
    return ResponseEntity.ok("Started Steam Market Request.");
  }

  @Scheduled(cron = "${scheduler.request}")
  private void startSteamMarketQueryScheduled() {
    log.info("Starting scheduled Steam Market Request.");
    CompletableFuture.runAsync(steamInventoryTrackerService::requestItemsSync);
  }

  @GetMapping(path = "/getTotalValue")
  public ResponseEntity<Double> getTotalInventoryValue() {

    final List<BuyInfo> allBuyInfos = buyInfoService.getAllBuyInfos();
    double total = 0.0;
    for (final BuyInfo buyInfo : allBuyInfos) {
      final String itemName = buyInfo.getItemName();
      final Item itemByName = itemService.getItemByName(itemName);
      try {
        total += itemByName.getLatestPrice().getPrice() * buyInfo.getAmount();
      } catch (final PriceHistoryException e) {
        log.error(e.getMessage());
      }
    }
    return ResponseEntity.ok(total);
  }

  @GetMapping(path = "/exportAsCSV", produces = "text/csv")
  public ResponseEntity<String> getAllCurrentItemsAsCSV() {
    log.info("Exporting latest prices as CSV.");
    final List<Item> items = itemService.getAllItems();
    final String csv = CSVExporter.createCSV(items);
    return ResponseEntity.ok(csv);
  }

  @GetMapping(path = "/priceTrend")
  public ResponseEntity<PriceTrend> getPriceTrendForItem(
      @RequestParam final String name,
      @RequestParam final int timespan,
      @RequestParam final ChronoUnit chronoUnit) {
    log.info("Returning PriceTrend for last {} {} of Item '{}'", timespan, chronoUnit, name);
    final PriceTrend priceTrend = itemService.getPriceTrendForItem(name, timespan, chronoUnit);
    return ResponseEntity.ok(priceTrend);
  }

  @GetMapping(path = "/priceHistory")
  public ResponseEntity<List<Price>> getPriceHistoryForItem(@RequestParam final String name) {
    log.info("Returning averaged price history for Item '{}'", name);
    final List<Price> priceHistoryForItem = itemService.getPriceHistoryForItem(name);
    return ResponseEntity.ok(priceHistoryForItem);
  }
}
