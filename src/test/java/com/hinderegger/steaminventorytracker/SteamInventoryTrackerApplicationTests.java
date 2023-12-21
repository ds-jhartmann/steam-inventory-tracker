package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.hinderegger.steaminventorytracker.controller.SteamInventoryTrackerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SteamInventoryTrackerApplicationTests {

  @Autowired private SteamInventoryTrackerController steamInventoryTrackerController;

  @Test
  void contextLoads() {
    assertThat(steamInventoryTrackerController).isNotNull();
  }
}
