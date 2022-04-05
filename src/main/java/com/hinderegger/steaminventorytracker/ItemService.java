package com.hinderegger.steaminventorytracker;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
            throw new IllegalStateException("Item \"" + name + "\" does not exist.");
        }
    }

    public Item getItemByName(String name) {
        final Optional<Item> itemByName = itemRepository.findById(name);
        return itemByName.orElseThrow(() -> new IllegalStateException("Item \"" + name + "\" does not exist."));
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
}
