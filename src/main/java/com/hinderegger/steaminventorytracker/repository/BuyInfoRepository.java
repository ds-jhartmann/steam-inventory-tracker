package com.hinderegger.steaminventorytracker.repository;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BuyInfoRepository extends MongoRepository<BuyInfo, String> {

  List<BuyInfo> findAllByItemName(String itemName);
}
