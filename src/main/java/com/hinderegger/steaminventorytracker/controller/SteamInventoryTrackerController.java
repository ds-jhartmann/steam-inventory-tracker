package com.hinderegger.steaminventorytracker.controller;

import static com.hinderegger.steaminventorytracker.SteamInventoryTrackerApplication.API_PATH;

import com.hinderegger.steaminventorytracker.CSVExporter;
import com.hinderegger.steaminventorytracker.PriceParser;
import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.service.BuyInfoService;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.SteamInventoryTrackerService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    List<BuyInfo> addedBuyInfos = buyInfoService.addBuyInfos(buyInfos);
    return ResponseEntity.ok(addedBuyInfos);
  }

  @PostMapping(path = "/addItems", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Item>> addNewItems(@RequestBody final List<Item> items) {
    final ArrayList<Item> returnItems = new ArrayList<>();
    for (final Item item : items) {
      returnItems.add(itemService.addItem(item));
    }
    return ResponseEntity.ok(returnItems);
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
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<List<Item>> getAllItems() {
    log.info("Returning all Items.");
    List<Item> allItems = itemService.getAllItems();
    return ResponseEntity.ok(allItems);
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
  @ResponseStatus(HttpStatus.OK)
  public double getTotalInventoryValue() {

    final List<BuyInfo> allBuyInfos = buyInfoService.getAllBuyInfos();
    double total = 0.0;
    for (final BuyInfo buyInfo : allBuyInfos) {
      final String itemName = buyInfo.getItemName();
      final Item itemByName = itemService.getItemByName(itemName);
      try {
        total += PriceParser.getLatestPriceFromItem(itemByName).getPrice() * buyInfo.getAmount();
      } catch (final Exception e) {
        log.error(e.getMessage());
      }
    }
    return total;
  }

  @GetMapping(path = "/exportAsCSV", produces = "text/csv")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> getAllCurrentItemsAsCSV() {
    log.info("Exporting latest prices as CSV.");
    List<Item> items = itemService.getAllItems();
    String csv = CSVExporter.createCSV(items);
    return ResponseEntity.ok(csv);
  }
}
