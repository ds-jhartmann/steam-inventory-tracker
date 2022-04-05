package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.repository.BuyInfoRepository;
import com.hinderegger.steaminventorytracker.model.BuyInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BuyInfoService {

    private final BuyInfoRepository buyInfoRepository;

    public BuyInfo addBuyInfo(BuyInfo buyInfo) {
        return buyInfoRepository.insert(buyInfo);
    }

    public List<BuyInfo> getBuyInfoByName(String name) {
        final List<BuyInfo> itemByName = buyInfoRepository.findAllByItemName(name);
        if (itemByName.isEmpty()) {
            throw new IllegalStateException("BuyInfo \"" + name + "\" does not exist.");
        }
        return itemByName;
    }

    public List<BuyInfo> getAllItems() {
        return buyInfoRepository.findAll();
    }
}
