package com.hinderegger.steaminventorytracker.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class SteamInventoryTrackerServiceTest {

  private SteamInventoryTrackerService testee;
  private HttpClient httpClient;
  private ItemRepository itemRepository;
  private SteamMarketAPICallerService steamMock;

  @BeforeEach
  void setUp() {
    steamMock = mock(SteamMarketAPICallerService.class);
    var steamConfig = new SteamConfiguration();
    steamConfig.setBaseurl("http://local.test");
    steamConfig.setPath("/test?query=");
    itemRepository = mock(ItemRepository.class);
    httpClient = mock(HttpClient.class);
    testee = new SteamInventoryTrackerService(itemRepository, steamMock, steamConfig, httpClient);

    Price price = new Price(0.1, 0.11, LocalDateTime.of(2023, 12, 20, 15, 0, 0));
    ArrayList<Price> priceHistory = new ArrayList<>(List.of(price));
    Item item = new Item("Test Item 1", priceHistory);
    ArrayList<Item> itemList = new ArrayList<>(List.of(item, item));

    when(itemRepository.findAll()).thenReturn(itemList);
    when(itemRepository.save(any())).thenReturn(null);
  }

  @Test
  void requestItems() {
    when(steamMock.getPriceForItem(any()))
        .thenReturn(
            Mono.just(
                "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}"));

    assertThatNoException().isThrownBy(() -> testee.requestItems());
  }

  @Test
  void requestItemsSync() throws IOException, InterruptedException {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body())
        .thenReturn(
            "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}");

    when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockResponse);

    assertThatNoException().isThrownBy(() -> testee.requestItemsSync());
  }
}
