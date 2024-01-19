package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.repository.ItemRepository;
import com.hinderegger.steaminventorytracker.service.SteamInventoryTrackerService;
import com.hinderegger.steaminventorytracker.service.SteamMarketAPIClient;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

@DataMongoTest
@Testcontainers
@ContextConfiguration(classes = MongoDBTestContainerConfig.class)
class RepositoryTest {

  @Autowired MongoTemplate mongoTemplate;

  @Autowired private ItemRepository itemRepository;
  private SteamInventoryTrackerService testee;
  private SteamMarketAPIClient steamMock;
  private HttpClient httpClientMock;

  @BeforeEach
  void setUp() {
    steamMock = mock(SteamMarketAPIClient.class);
    SteamConfiguration steamConfig = new SteamConfiguration();
    steamConfig.setBaseurl("http://local.test");
    steamConfig.setPath("/test?query=");
    steamConfig.setSleepDuration(1);

    httpClientMock = mock(HttpClient.class);

    testee =
        new SteamInventoryTrackerService(itemRepository, steamMock, steamConfig, httpClientMock);
  }

  @AfterEach
  void tearDown() {
    itemRepository.deleteAll();
  }

  @Test
  void shouldRequestItem() {
    // Arrange
    mongoTemplate.insert(new Item("Test 1", List.of()));
    String data =
        """
              {"success":true,"lowest_price":"5,79€","volume":"3,990","median_price":"5,61€"}""";
    when(steamMock.getPriceForItem(any())).thenReturn(Mono.just(data));

    // Act
    testee.requestItems();

    // Assert
    List<Item> items = itemRepository.findAll();
    assertThat(items).hasSize(1);
    assertThat(items.getFirst().getItemName()).isEqualTo("Test 1");
    assertThat(items.getFirst().getPriceHistory()).isNotEmpty();
    assertThat(items.getFirst().getPriceHistory().getFirst().getPrice()).isEqualTo(5.79);
    assertThat(items.getFirst().getPriceHistory().getFirst().getMedian()).isEqualTo(5.61);
    verify(steamMock, times(1)).getPriceForItem(any());
  }

  @Test
  void shouldRequestAllItems() {
    // Arrange
    Item item1 = new Item("Test 1", List.of());
    Item item2 = new Item("Test 2", List.of());
    mongoTemplate.insert(item1);
    mongoTemplate.insert(item2);
    String data1 =
        "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}";
    String data2 =
        "{\"success\":true,\"lowest_price\":\"0,99€\",\"volume\":\"3,990\",\"median_price\":\"0,65€\"}";
    when(steamMock.getPriceForItem(any()))
        .thenReturn(Mono.just(data1))
        .thenReturn(Mono.just(data2));

    // Act
    testee.requestItems();

    // Assert
    List<Item> items = itemRepository.findAll();
    assertThat(items).hasSize(2);
    Optional<Item> storedItem1 = itemRepository.findById("Test 1");
    Optional<Item> storedItem2 = itemRepository.findById("Test 2");
    assertThat(storedItem1).isPresent();
    assertThat(storedItem2).isPresent();

    verify(steamMock, times(2)).getPriceForItem(any());
  }

  @Test
  void shouldRequestItemSync() throws IOException, InterruptedException {
    // Arrange
    Item item1 = new Item("Test 1", List.of());
    Item item2 = new Item("Test 2", List.of());
    mongoTemplate.insert(item1);
    mongoTemplate.insert(item2);
    String data1 =
        "{\"success\":true,\"lowest_price\":\"5,79€\",\"volume\":\"3,990\",\"median_price\":\"5,61€\"}";
    String data2 =
        "{\"success\":true,\"lowest_price\":\"0,99€\",\"volume\":\"3,990\",\"median_price\":\"0,65€\"}";
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(data1).thenReturn(data2);

    when(httpClientMock.send(any(), eq(HttpResponse.BodyHandlers.ofString())))
        .thenReturn(mockResponse);

    // Act
    testee.requestItemsSync();

    // Assert
    List<Item> items = itemRepository.findAll();
    assertThat(items).hasSize(2);
    Optional<Item> storedItem1 = itemRepository.findById("Test 1");
    Optional<Item> storedItem2 = itemRepository.findById("Test 2");
    assertThat(storedItem1).isPresent();
    assertThat(storedItem2).isPresent();

    verify(httpClientMock, times(2)).send(any(), any());
  }
}
