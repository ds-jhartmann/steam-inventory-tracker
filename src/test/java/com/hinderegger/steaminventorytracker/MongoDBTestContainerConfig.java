package com.hinderegger.steaminventorytracker;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@Configuration
@EnableMongoRepositories
public class MongoDBTestContainerConfig {
  @Container
  public static MongoDBContainer mongoDBContainer =
      new MongoDBContainer("mongo:6.0").withExposedPorts(27017).withReuse(true);

  static {
    mongoDBContainer.start();
    var mappedPort = mongoDBContainer.getMappedPort(27017);
    System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
    System.setProperty("testcontainers.reuse.enable", "true");
  }
}
