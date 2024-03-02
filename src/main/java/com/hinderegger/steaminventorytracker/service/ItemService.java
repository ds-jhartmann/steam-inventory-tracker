package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.model.PriceTrend;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
@Slf4j
public class ItemService {

  private final ItemRepository itemRepository;

  public Item addItem(final Item item) {
    final String itemName = item.getItemName();
    if (itemRepository.findById(itemName).isEmpty()) {
      log.info("Adding Item '{}'.", itemName);
      return itemRepository.insert(item);
    } else {
      log.info("Item '{}' already present.", itemName);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Item already exists for name " + itemName);
    }
  }

  public Item addItem(final String itemName) {
    final Item item = new Item(itemName, List.of());
    return addItem(item);
  }

  public List<Item> addItems(final List<Item> items) {
    final ArrayList<Item> returnItems = new ArrayList<>();
    for (final Item item : items) {
      returnItems.add(this.addItem(item));
    }
    return returnItems;
  }

  public Item updatePriceForItem(final String name, final double price, final double median) {
    final Optional<Item> itemByName = itemRepository.findById(name);
    if (itemByName.isPresent()) {
      final Price price1 = new Price(price, median, LocalDateTime.now());
      final Item item = itemByName.get();
      item.addPrice(price1);
      return itemRepository.save(item);
    } else {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Item '" + name + "' does not exist.");
    }
  }

  public Item getItemByName(final String name) {
    final Optional<Item> itemByName = itemRepository.findById(name);
    return itemByName.orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No job exists with id " + name));
  }

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }

  public PriceTrend getPriceTrendForItem(
      final String name, final int timespan, final ChronoUnit chronoUnit) {
    final Item item = getItemByName(name);
    try {
      return item.calculatePriceTrendByDay(timespan, chronoUnit);
    } catch (PriceHistoryException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public List<Price> getPriceHistoryForItem(final String name) {
    return getItemByName(name).calculateAverageAndMedianPricesPerDay();
  }
}
