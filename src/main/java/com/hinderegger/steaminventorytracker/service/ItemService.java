package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item addItem(Item item) {
        return itemRepository.insert(item);
    }

    public Item updatePriceForItem(String name, double price, double median) {
        final Optional<Item> itemByName = itemRepository.findById(name);
        if (itemByName.isPresent()) {
            final Price price1 = new Price(price, median, LocalDateTime.now());
            final Item item = itemByName.get();
            item.addPrice(price1);
            return itemRepository.save(item);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item \"" + name + "\" does not exist.");
        }
    }

    public Item getItemByName(String name) {
        final Optional<Item> itemByName = itemRepository.findById(name);
        return itemByName.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No job exists with id " + name));
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
}
