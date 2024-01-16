package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import java.time.LocalDateTime;
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

  public Item addItem(Item item) {
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

  public Item addItem(String itemName) {
    final Item item = new Item(itemName, List.of());
    return addItem(item);
  }

  public List<Item> addItems(List<Item> items) {
    return itemRepository.insert(items);
  }

  public Item updatePriceForItem(String name, double price, double median) {
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

  public Item getItemByName(String name) {
    final Optional<Item> itemByName = itemRepository.findById(name);
    return itemByName.orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No job exists with id " + name));
  }

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }
}
