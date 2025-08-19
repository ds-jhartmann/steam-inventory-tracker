package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.persistence.BuyInfoEntity;
import com.hinderegger.steaminventorytracker.repository.BuyInfoRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BuyInfoService {

  private final BuyInfoRepository buyInfoRepository;

  public List<BuyInfo> addBuyInfos(List<BuyInfo> buyInfos) {
    var entities = buyInfos.stream()
        .map(b -> new BuyInfoEntity(b.getItemName(), b.getAmount(), b.getBuyPrice()))
        .toList();
    return buyInfoRepository.saveAll(entities).stream()
        .map(e -> new BuyInfo(e.getItemName(), e.getAmount(), e.getBuyPrice()))
        .toList();
  }

  public BuyInfo addBuyInfo(final String name, final Integer amount, final Double buyPrice) {
    var saved = buyInfoRepository.save(new BuyInfoEntity(name, amount, buyPrice));
    return new BuyInfo(saved.getItemName(), saved.getAmount(), saved.getBuyPrice());
  }

  public List<BuyInfo> getAllBuyInfos() {
    return buyInfoRepository.findAll().stream()
        .map(e -> new BuyInfo(e.getItemName(), e.getAmount(), e.getBuyPrice()))
        .toList();
  }
}
