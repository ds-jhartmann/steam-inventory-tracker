package com.hinderegger.steaminventorytracker.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class Item {
    @Id
    private final String itemName;
    private final int amount;
    private final double buyPrice;
    private final List<Price> priceHistory;
}
