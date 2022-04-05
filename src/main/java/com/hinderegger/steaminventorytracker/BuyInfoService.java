package com.hinderegger.steaminventorytracker;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
