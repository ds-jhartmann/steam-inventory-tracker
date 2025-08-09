package com.hinderegger.steaminventorytracker.repository;

import com.hinderegger.steaminventorytracker.model.Item;
import java.util.List;

public interface ItemRepositoryCustom {
  List<Item> findAllWithLatestPrice();
}
