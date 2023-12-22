package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.repository.BuyInfoRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BuyInfoService {

  private final BuyInfoRepository buyInfoRepository;

  public List<BuyInfo> addBuyInfos(List<BuyInfo> buyInfos) {
    return buyInfoRepository.insert(buyInfos);
  }

  public BuyInfo addBuyInfo(final String name, final Integer amount, final Double buyPrice) {
    final BuyInfo item = new BuyInfo(name, amount, buyPrice);
    return buyInfoRepository.insert(item);
  }

  public List<BuyInfo> getAllBuyInfos() {
    return buyInfoRepository.findAll();
  }
}
