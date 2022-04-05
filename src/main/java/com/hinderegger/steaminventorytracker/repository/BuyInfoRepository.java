package com.hinderegger.steaminventorytracker.repository;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BuyInfoRepository extends MongoRepository<BuyInfo, String> {

    List<BuyInfo> findAllByItemName(String itemName);
}
