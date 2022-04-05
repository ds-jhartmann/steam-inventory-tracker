package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SteamInventoryTrackerService {
    private final ItemRepository itemRepository;
    private final SteamMarketAPICallerService steamMarketAPICallerService;

    public SteamInventoryTrackerService(ItemRepository itemRepository, SteamMarketAPICallerService steamMarketAPICallerService) {
        this.itemRepository = itemRepository;
        this.steamMarketAPICallerService = steamMarketAPICallerService;
    }

    public void requestItems() {
        final List<Item> all = itemRepository.findAll();
        Collections.shuffle(all);
        final long started = System.currentTimeMillis();
        all.forEach(this::requestItem);
        log.info("Elapsed time in seconds: " + (System.currentTimeMillis() - started) / 1000d);
    }

    private void requestItem(Item item) {
        final Mono<String> priceMono = callSteamAPI(item);

        priceMono.log().subscribe(priceResponse -> {
            log.info("Price is: " + priceResponse);
            final JSONObject jsonObject = new JSONObject(priceResponse);
            final String median_price = jsonObject.getString("median_price").replace("€", "").replace(",", ".");
            final String lowest_price = jsonObject.getString("lowest_price").replace("€", "").replace(",", ".");

            final Price newValue = new Price(Double.parseDouble(lowest_price), Double.parseDouble(median_price), LocalDateTime.now());
            item.addPrice(newValue);
            itemRepository.save(item);
        }, error -> {
            log.error("Could not get price");
            throw new IllegalStateException("error while retrieving value for item: " + item.getItemName());
        }, () -> log.info("Mono consumed."));
    }

    private Mono<String> callSteamAPI(Item item) {
        return steamMarketAPICallerService.getPriceForItem(item);
    }
}
