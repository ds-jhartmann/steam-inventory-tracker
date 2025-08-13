package com.hinderegger.steaminventorytracker.repository.jpa;

import com.hinderegger.steaminventorytracker.persistence.ItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<ItemEntity, String> {
    @Query(
      """
        SELECT i.itemName, p.price, p.median, p.timestamp
            FROM ItemEntity i
            JOIN PriceEntity p ON p.item = i
            WHERE p.timestamp = (
                SELECT MAX(ph.timestamp)
                FROM PriceEntity ph
                WHERE ph.item = i
            )
        """)
    List<ItemWithLatestPriceDTO> findAllWithLatestPrice();

    record ItemWithLatestPriceDTO(String itemName, double price, double median, java.time.LocalDateTime timestamp) {}
}
