package com.hinderegger.steaminventorytracker.repository;

import com.hinderegger.steaminventorytracker.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String> {}
