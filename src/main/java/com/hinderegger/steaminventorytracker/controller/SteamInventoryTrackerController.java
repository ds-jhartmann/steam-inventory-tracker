package com.hinderegger.steaminventorytracker.controller;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.service.BuyInfoService;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.SteamInventoryTrackerService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/items")
@AllArgsConstructor
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
        steamInventoryTrackerService.requestItemsSync();
        return "Successful";
    }
}