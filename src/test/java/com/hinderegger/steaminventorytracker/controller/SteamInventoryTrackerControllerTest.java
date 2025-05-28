package com.hinderegger.steaminventorytracker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hinderegger.steaminventorytracker.model.BuyInfo;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.model.Price;
import com.hinderegger.steaminventorytracker.model.PriceTrend;
import com.hinderegger.steaminventorytracker.service.BuyInfoService;
import com.hinderegger.steaminventorytracker.service.ItemService;
import com.hinderegger.steaminventorytracker.service.PriceHistoryException;
import com.hinderegger.steaminventorytracker.service.SteamInventoryTrackerService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SteamInventoryTrackerControllerTest {

  private SteamInventoryTrackerService steamInventoryTrackerService;
  private ItemService itemService;
  private BuyInfoService buyInfoService;
  private SteamInventoryTrackerController controller;

  private Item testItem;
  private BuyInfo testBuyInfo;
  private List<Price> testPriceHistory;
  private PriceTrend testPriceTrend;

  @BeforeEach
  void setUp() {
    // Set up mocks
    steamInventoryTrackerService = mock(SteamInventoryTrackerService.class);
    itemService = mock(ItemService.class);
    buyInfoService = mock(BuyInfoService.class);
    
    // Create controller with mocks
    controller = new SteamInventoryTrackerController(
        steamInventoryTrackerService, itemService, buyInfoService);
    
    // Set up test data
    testPriceHistory = new ArrayList<>();
    testPriceHistory.add(new Price(10.0, 11.0, LocalDateTime.now()));
    testItem = new Item("Test Item", testPriceHistory);
    testBuyInfo = new BuyInfo("Test Item", 5, 9.99);
    testPriceTrend = new PriceTrend(1.0, 0.1, 1.5, 0.15);
  }

  @Test
  void shouldAddNewItem() {
    // Arrange
    when(itemService.addItem(anyString())).thenReturn(testItem);

    // Act
    ResponseEntity<Item> response = controller.addNewItem("Test Item");

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(testItem);
    verify(itemService, times(1)).addItem("Test Item");
  }

  @Test
  void shouldRegisterBuyInfo() {
    // Arrange
    when(buyInfoService.addBuyInfo(anyString(), anyInt(), anyDouble())).thenReturn(testBuyInfo);

    // Act
    ResponseEntity<BuyInfo> response = controller.registerBuyInfo("Test Item", 5, 9.99);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(testBuyInfo);
    verify(buyInfoService, times(1)).addBuyInfo("Test Item", 5, 9.99);
  }

  @Test
  void shouldRegisterBuyInfos() {
    // Arrange
    List<BuyInfo> buyInfos = List.of(testBuyInfo);
    when(buyInfoService.addBuyInfos(any())).thenReturn(buyInfos);

    // Act
    ResponseEntity<List<BuyInfo>> response = controller.registerBuyInfos(buyInfos);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(buyInfos);
    verify(buyInfoService, times(1)).addBuyInfos(buyInfos);
  }

  @Test
  void shouldAddItems() {
    // Arrange
    List<Item> items = List.of(testItem);
    when(itemService.addItem(any(Item.class))).thenReturn(testItem);

    // Act
    ResponseEntity<List<Item>> response = controller.addNewItems(items);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0)).isEqualTo(testItem);
    verify(itemService, times(1)).addItem(any(Item.class));
  }

  @Test
  void shouldUpdatePrice() {
    // Arrange
    when(itemService.updatePriceForItem(anyString(), anyDouble(), anyDouble())).thenReturn(testItem);

    // Act
    ResponseEntity<Item> response = controller.updatePrice("Test Item", 10.0, 11.0);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(testItem);
    verify(itemService, times(1)).updatePriceForItem("Test Item", 10.0, 11.0);
  }

  @Test
  void shouldGetItemByName() {
    // Arrange
    when(itemService.getItemByName(anyString())).thenReturn(testItem);

    // Act
    ResponseEntity<Item> response = controller.getItemByName("Test Item");

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(testItem);
    verify(itemService, times(1)).getItemByName("Test Item");
  }

  @Test
  void shouldGetAllItems() {
    // Arrange
    List<Item> items = List.of(testItem);
    when(itemService.getAllItems()).thenReturn(items);

    // Act
    ResponseEntity<List<Item>> response = controller.getAllItems();

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(items);
    verify(itemService, times(1)).getAllItems();
  }

  @Test
  void shouldStartSteamMarketQuery() {
    // Arrange
    doNothing().when(steamInventoryTrackerService).requestItemsSync();

    // Act
    ResponseEntity<String> response = controller.startSteamMarketQuery();

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("Started Steam Market Request.");
    // Note: We can't verify the async call directly
  }

  @Test
  void shouldGetTotalInventoryValue() throws PriceHistoryException {
    // Arrange
    List<BuyInfo> buyInfos = List.of(testBuyInfo);
    when(buyInfoService.getAllBuyInfos()).thenReturn(buyInfos);
    when(itemService.getItemByName(anyString())).thenReturn(testItem);

    // Act
    ResponseEntity<Double> response = controller.getTotalInventoryValue();

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(50.0); // 10.0 (price) * 5 (amount) = 50.0
    verify(buyInfoService, times(1)).getAllBuyInfos();
    verify(itemService, times(1)).getItemByName("Test Item");
  }

  @Test
  void shouldHandlePriceHistoryException() throws PriceHistoryException {
    // Arrange
    List<BuyInfo> buyInfos = List.of(testBuyInfo);
    when(buyInfoService.getAllBuyInfos()).thenReturn(buyInfos);
    
    Item itemWithoutPrice = new Item("Test Item", new ArrayList<>());
    when(itemService.getItemByName(anyString())).thenReturn(itemWithoutPrice);

    // Act
    ResponseEntity<Double> response = controller.getTotalInventoryValue();

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(0.0); // Exception should result in 0.0
    verify(buyInfoService, times(1)).getAllBuyInfos();
    verify(itemService, times(1)).getItemByName("Test Item");
  }

  @Test
  void shouldExportAsCSV() {
    // Arrange
    List<Item> items = List.of(testItem);
    when(itemService.getAllItems()).thenReturn(items);

    // Act
    ResponseEntity<String> response = controller.getAllCurrentItemsAsCSV();

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
    verify(itemService, times(1)).getAllItems();
  }

  @Test
  void shouldGetPriceTrendForItem() {
    // Arrange
    when(itemService.getPriceTrendForItem(anyString(), anyInt(), any(ChronoUnit.class)))
        .thenReturn(testPriceTrend);

    // Act
    ResponseEntity<PriceTrend> response = controller.getPriceTrendForItem(
        "Test Item", 7, ChronoUnit.DAYS);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(testPriceTrend);
    verify(itemService, times(1)).getPriceTrendForItem("Test Item", 7, ChronoUnit.DAYS);
  }

  @Test
  void shouldGetPriceHistoryForItem() {
    // Arrange
    when(itemService.getPriceHistoryForItem(anyString())).thenReturn(testPriceHistory);

    // Act
    ResponseEntity<List<Price>> response = controller.getPriceHistoryForItem("Test Item");

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(testPriceHistory);
    verify(itemService, times(1)).getPriceHistoryForItem("Test Item");
  }
}