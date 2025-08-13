package com.hinderegger.steaminventorytracker.service.mapper;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.persistence.ItemEntity;
import com.hinderegger.steaminventorytracker.persistence.PriceEntity;
import com.hinderegger.steaminventorytracker.repository.jpa.ItemRepository;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {
  public static ItemEntity toEntity(Item model) {
    ItemEntity e = new ItemEntity(model.getItemName());
    if (model.getPriceHistory() != null && !model.getPriceHistory().isEmpty()) {
      List<PriceEntity> ph = new ArrayList<>();
      e.setPriceHistory(ph);
      for (Price p : model.getPriceHistory()) {
        ph.add(new PriceEntity(e, p.price(), p.median(), p.timestamp()));
      }
    }
    return e;
  }

  public static Item toModel(ItemEntity e) {
    List<Price> prices =
        e.getPriceHistory() == null
            ? List.of()
            : e.getPriceHistory().stream()
                .map(pe -> new Price(pe.getPrice(), pe.getMedian(), pe.getTimestamp()))
                .toList();
    return new Item(e.getItemName(), prices);
  }

  public static Item toModel(ItemRepository.ItemWithLatestPriceDTO dto) {
      return new Item(dto.itemName(), List.of(new Price(dto.price(), dto.median(), dto.timestamp())));
  }

  public static Price toModel(PriceEntity p) {
    return new Price(p.getPrice(), p.getMedian(), p.getTimestamp());
  }
}
