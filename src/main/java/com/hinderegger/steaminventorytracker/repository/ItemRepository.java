package com.hinderegger.steaminventorytracker.repository;

import com.hinderegger.steaminventorytracker.persistence.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, String> {
    @Query(
            value = """
                      SELECT DISTINCT ON (p.item_name)
                             p.item_name AS "itemName",
                             p.price     AS price,
                             p.median    AS median,
                             p.timestamp AS timestamp
                      FROM price_history p
                      ORDER BY p.item_name, p.timestamp DESC
                    """,
            nativeQuery = true)
    List<LatestPriceView> findAllWithLatestPriceFast();

    // Back-compat: same name as before, but implemented via the interface projection
    default List<ItemWithLatestPriceDTO> findAllWithLatestPrice() {
        return findAllWithLatestPriceFast().stream()
                .map(v -> new ItemWithLatestPriceDTO(
                        v.getItemName(), v.getPrice(), v.getMedian(), v.getTimestamp()))
                .toList();
    }

    interface LatestPriceView {
        String getItemName();

        double getPrice();

        double getMedian();

        LocalDateTime getTimestamp();
    }

    record ItemWithLatestPriceDTO(String itemName, double price, double median, LocalDateTime timestamp) {
    }
}
