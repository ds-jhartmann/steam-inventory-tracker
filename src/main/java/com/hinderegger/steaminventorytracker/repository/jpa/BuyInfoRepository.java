package com.hinderegger.steaminventorytracker.repository.jpa;

import com.hinderegger.steaminventorytracker.persistence.BuyInfoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyInfoRepository extends JpaRepository<BuyInfoEntity, UUID> {
  List<BuyInfoEntity> findAllByItemName(String itemName);
}
