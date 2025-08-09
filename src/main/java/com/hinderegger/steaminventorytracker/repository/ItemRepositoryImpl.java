package com.hinderegger.steaminventorytracker.repository;

import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Repository
public class ItemRepositoryImpl implements ItemRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public ItemRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<Item> findAllWithLatestPrice() {
    // Build a projection to alias _id to itemName and compute the last element of priceHistory
    ProjectionOperation projection =
        project()
            .and("_id").as("itemName")
            .andExpression("arrayElemAt(priceHistory, -1)").as("latest");

    Aggregation agg = Aggregation.newAggregation(projection);

    AggregationResults<Document> results =
        mongoTemplate.aggregate(agg, mongoTemplate.getCollectionName(Item.class), Document.class);

    return results.getMappedResults().stream()
        .map(doc -> {
          String itemName = doc.getString("itemName");
          Document latest = (Document) doc.get("latest");
          double price = 0.0;
          double median = 0.0;
          LocalDateTime ts = LocalDateTime.now();
          if (latest != null) {
            Number p = (Number) latest.get("price");
            Number m = (Number) latest.get("median");
            Object t = latest.get("timestamp");
            if (p != null) price = p.doubleValue();
            if (m != null) median = m.doubleValue();
            if (t instanceof Date d) {
              ts = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
          }
          return new Item(itemName, List.of(new Price(price, median, ts)));
        })
        .toList();
  }
}
