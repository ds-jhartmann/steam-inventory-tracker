package com.hinderegger.steaminventorytracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.persistence.ItemEntity;
import com.hinderegger.steaminventorytracker.persistence.PriceEntity;
import com.hinderegger.steaminventorytracker.repository.jpa.ItemRepository;
import com.hinderegger.steaminventorytracker.repository.jpa.PriceRepository;
import com.hinderegger.steaminventorytracker.service.*;
import com.hinderegger.steaminventorytracker.service.mapper.ItemMapper;
import com.hinderegger.steaminventorytracker.service.strategy.AsyncSteamRequestStrategy;
import com.hinderegger.steaminventorytracker.service.strategy.SteamApiQueryException;
import com.hinderegger.steaminventorytracker.service.strategy.SyncSteamRequestStrategy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

/**
 * Integration test for the SteamInventoryTrackerService with PostgreSQL. This test uses the
 * Strategy pattern to test both synchronous and asynchronous item requests.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresTestContainerConfig.class)
class RepositoryTest {

  public static final Item ITEM_1 = new Item("Test 1", List.of());
  public static final Item ITEM_2 = new Item("Test 2", List.of());
  @Autowired private ItemRepository itemRepository;
  @Autowired private PriceRepository priceRepository;

  private SteamInventoryTrackerService testee;
  private AsyncSteamRequestStrategy asyncStrategy;
  private SyncSteamRequestStrategy syncStrategy;
  private ItemService itemService;

  @BeforeEach
  void setUp() throws SteamApiQueryException, InterruptedException {

    final String dataItem1 =
        """
                {
                    "success": true,
                    "lowest_price": "5,79€",
                    "volume": "1,684",
                    "median_price": "5,61€"
                }
                """;
    final String dataItem2 =
        """
                {
                    "success": true,
                    "lowest_price": "0,99€",
                    "volume": "1,684",
                    "median_price": "0,65"
                }
                """;
    var mockSteamClient = mock(SteamMarketAPIClient.class);
    when(mockSteamClient.getPriceForItem(eq(ITEM_1))).thenReturn(Mono.just(dataItem1));
    when(mockSteamClient.getPriceForItem(eq(ITEM_2))).thenReturn(Mono.just(dataItem2));

    var mockSteamHttpClient = mock(SteamMarketHttpClient.class);
    when(mockSteamHttpClient.getStringHttpResponse(eq(ITEM_1))).thenReturn(dataItem1);
    when(mockSteamHttpClient.getStringHttpResponse(eq(ITEM_2))).thenReturn(dataItem2);

    var steamConfig = mock(SteamConfiguration.class);
    when(steamConfig.getSleepDuration()).thenReturn(0);

    var timeProvider = new DefaultTimeProvider();
    var priceService = new PriceService();

    itemService = new ItemService(itemRepository, timeProvider, priceService, priceRepository);
    asyncStrategy = spy(new AsyncSteamRequestStrategy(mockSteamClient, itemService, timeProvider));
    syncStrategy =
        spy(
            new SyncSteamRequestStrategy(
                steamConfig, timeProvider, itemService, mockSteamHttpClient));
    testee = new SteamInventoryTrackerService(itemService, asyncStrategy, syncStrategy);
  }

  @AfterEach
  void tearDown() {
    // Delete only the specific items used in tests
    itemRepository.deleteById("Test 1");
    itemRepository.deleteById("Test 2");
  }

  @Test
  void shouldRequestItemAsync() {
    // Arrange
    final Item item = new Item("Test 1", List.of());
    itemService.addItem(item);

    // Act
    testee.requestItems();

    // Assert
    List<ItemEntity> items = itemRepository.findAll();
    assertThat(items).hasSize(1);
    assertThat(items.getFirst().getItemName()).isEqualTo("Test 1");
    final List<PriceEntity> allByItemEntity = priceRepository.findAllByItem(items.getFirst());
    assertThat(allByItemEntity).isNotEmpty();
    assertThat(allByItemEntity.getFirst().getPrice()).isEqualTo(5.79);
    assertThat(allByItemEntity.getFirst().getMedian()).isEqualTo(5.61);

    verify(asyncStrategy, times(1)).requestItems(any());
    verifyNoInteractions(syncStrategy);
  }

  @Test
  void shouldRequestAllItemsAsync() {
    // Arrange
    Item item1 = new Item("Test 1", List.of());
    Item item2 = new Item("Test 2", List.of());
    itemService.addItem(item1);
    itemService.addItem(item2);

    // Act
    testee.requestItems();

    // Assert
    List<ItemEntity> items = itemRepository.findAll();
    assertThat(items).hasSize(2);
    Optional<Item> storedItem1 = itemRepository.findById("Test 1").map(ItemMapper::toModel);
    Optional<Item> storedItem2 = itemRepository.findById("Test 2").map(ItemMapper::toModel);
    assertThat(storedItem1).isPresent();
    assertThat(storedItem2).isPresent();

    // Verify the correct strategy was used
    verify(asyncStrategy, times(1)).requestItems(any());
    verifyNoInteractions(syncStrategy);
  }

  @Test
  void shouldRequestItemSync() {
    // Arrange
    itemRepository.save(ItemMapper.toEntity(ITEM_1));
    itemRepository.save(ItemMapper.toEntity(ITEM_2));

    // Act
    testee.requestItemsSync();

    // Assert
    List<ItemEntity> items = itemRepository.findAll();
    assertThat(items).hasSize(2);
    Optional<ItemEntity> storedItem1 = itemRepository.findById("Test 1");
    Optional<ItemEntity> storedItem2 = itemRepository.findById("Test 2");
    assertThat(storedItem1).isPresent();
    assertThat(storedItem2).isPresent();

    // Verify the correct strategy was used
    verify(syncStrategy, times(1)).requestItems(any());
    verifyNoInteractions(asyncStrategy);
  }
}
