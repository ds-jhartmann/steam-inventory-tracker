package com.hinderegger.steaminventorytracker.repository;

import com.hinderegger.steaminventorytracker.persistence.ItemEntity;
import com.hinderegger.steaminventorytracker.persistence.PriceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<PriceEntity, Long> {
  List<PriceEntity> findAllByItem(ItemEntity itemEntity);
}
