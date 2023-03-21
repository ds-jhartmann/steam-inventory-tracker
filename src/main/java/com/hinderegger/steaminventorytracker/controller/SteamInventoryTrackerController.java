package com.hinderegger.steaminventorytracker.controller;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.service.BuyInfoService;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.SteamInventoryTrackerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.hinderegger.steaminventorytracker.configuration.Constants.API_PATH;

@RestController
@RequestMapping(path = API_PATH)
@AllArgsConstructor
@Slf4j
public class SteamInventoryTrackerController {

    private final SteamInventoryTrackerService steamInventoryTrackerService;
    private final ItemService itemService;
    private final BuyInfoService buyInfoService;

    @PostMapping(path = "/addItem")
    public @ResponseStatus
    Item addNewItem(@RequestParam String name) {
        final Item item = new Item(name, List.of());

        return itemService.addItem(item);
    }

    @PostMapping(path = "/registerBuyInfo")
    public @ResponseStatus
    BuyInfo registerBuyInfo(@RequestParam String name,
                            @RequestParam Integer amount,
                            @RequestParam Double buyPrice) {
        final BuyInfo item = new BuyInfo(name, amount, buyPrice);

        return buyInfoService.addBuyInfo(item);
    }

    @PostMapping(path = "/registerBuyInfos")
    public @ResponseStatus
    List<BuyInfo> registerBuyInfos(@RequestBody List<BuyInfo> buyInfos) {
        final ArrayList<BuyInfo> returnBuyInfos = new ArrayList<>();
        for (BuyInfo buyInfo : buyInfos) {
            returnBuyInfos.add(buyInfoService.addBuyInfo(buyInfo));
        }

        return returnBuyInfos;
    }

    @PostMapping(path = "/addItems", consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseStatus
    List<Item> addNewItems(@RequestBody List<Item> items) {
        final ArrayList<Item> returnItems = new ArrayList<>();
        for (Item item : items) {
            returnItems.add(itemService.addItem(item));
        }

        return returnItems;
    }

    @PostMapping(path = "/updatePrice")
    public @ResponseBody
    Item updatePrice(@RequestParam String name,
                     @RequestParam double price,
                     @RequestParam double median) {

        return itemService.updatePriceForItem(name, price, median);
    }

    @GetMapping(path = "/getItem")
    public Item getItemByName(@RequestParam String name) {
        return itemService.getItemByName(name);
    }

    @GetMapping(path = "/all")
    public @ResponseBody
    Iterable<Item> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping(path = "/startSteamMarketRequest")
    public @ResponseBody
    String startSteamMarketQuery() {
        CompletableFuture.runAsync(steamInventoryTrackerService::requestItemsSync);
        return "Started Steam Market Request.";
    }

    @Scheduled(cron = "0 0 */3 * * *") // Every 3 hours
    private void startSteamMarketQueryScheduled() {
        log.info("Starting scheduled Steam Market Request");
        CompletableFuture.runAsync(steamInventoryTrackerService::requestItemsSync);
    }

    @GetMapping(path = "/getTotalValue")
    public @ResponseBody
    double getTotalInventoryValue() {

        final List<BuyInfo> allBuyInfos = buyInfoService.getAllBuyInfos();
        double total = 0.0;
        for (BuyInfo buyInfo : allBuyInfos) {
            final String itemName = buyInfo.getItemName();
            final Item itemByName = itemService.getItemByName(itemName);
            try {
                total += getLatestPriceFromItem(itemByName).getPrice() * buyInfo.getAmount();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return total;
    }

    private Price getLatestPriceFromItem(Item itemByName) throws ItemQueryException {
        final List<Price> priceHistory = itemByName.getPriceHistory();
        if (!priceHistory.isEmpty()) {
            priceHistory.sort(Comparator.comparing(Price::getTimestamp));
            final int lastIndex = priceHistory.size() - 1;
            return priceHistory.get(lastIndex);
        } else {
            throw new ItemQueryException("No price history for Item: " + itemByName.getItemName());
        }
    }

    @GetMapping(path = "/exportAsCSV", produces = "text/csv")
    @ResponseBody
    public String getAllCurrentItemsAsCSV() {
        List<String> result = new ArrayList<>();
        result.add("name,price,median");

        itemService.getAllItems().forEach(item -> {
            final String join = getCSVRow(item);
            result.add(join);
        });
        return String.join(",\n", result);
    }

    private String getCSVRow(Item item) {
        String latestPrice;
        String medianPrice;
        try {
            latestPrice = (getLatestPriceFromItem(item).getPrice() + "€").replace(".", ",");
        } catch (Exception e) {
            latestPrice = "0,00€";
            log.error(e.getMessage());
        }
        try {
            medianPrice = (getLatestPriceFromItem(item).getMedian() + "€").replace(".", ",");
        } catch (Exception e) {
            medianPrice = "0,00€";
            log.error(e.getMessage());
        }
        return String.join(",", item.getItemName(), "\"" + latestPrice + "\"", "\"" + medianPrice + "\"");
    }

}