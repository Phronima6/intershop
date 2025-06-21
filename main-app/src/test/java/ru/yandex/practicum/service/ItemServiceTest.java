package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.cache.ItemDetailCache;
import ru.yandex.practicum.cache.ItemListCache;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.ItemRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepo;
    
    @Mock
    private ImageRepository imageRepo;
    
    @Mock
    private ItemCacheService itemCacheService;

    @InjectMocks
    private ItemService itemService;

    private Item item1;
    private Item item2;
    private ItemDetailCache itemDetailCache1;
    private ItemListCache itemListCache1;
    private ItemListCache itemListCache2;

    @BeforeEach
    void setUp() {
        item1 = new Item();
        item1.setId(1);
        item1.setName("Test Item 1");
        item1.setDescription("Desc 1");
        item1.setPrice(10);
        item1.setAmount(5);

        item2 = new Item();
        item2.setId(2);
        item2.setName("Alpha Item 2");
        item2.setDescription("Desc 2");
        item2.setPrice(20);
        item2.setAmount(10);
        
        itemDetailCache1 = new ItemDetailCache(1, "Test Item 1", "Desc 1", 10.0, null);
        itemListCache1 = new ItemListCache(1, "Test Item 1", "Desc 1", 10.0);
        itemListCache2 = new ItemListCache(2, "Alpha Item 2", "Desc 2", 20.0);
        
        lenient().when(itemCacheService.invalidateItemCache(anyInt())).thenReturn(Mono.empty());
        
        lenient().when(itemCacheService.getOrCacheItemList(anyString(), any()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    if (key.contains("search_Test")) {
                        return Flux.just(itemListCache1);
                    } else {
                        return Flux.just(itemListCache1, itemListCache2);
                    }
                });
        
        lenient().when(itemCacheService.getOrCacheItemDetail(eq(1), any()))
                .thenReturn(Mono.just(itemDetailCache1));
        lenient().when(itemCacheService.getOrCacheItemDetail(eq(99), any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void getPaginatedItems_shouldReturnContentFromRepository() {
        int page = 1;
        int perPage = 10;
        
        when(itemRepo.findAll(any(Sort.class)))
                .thenReturn(Flux.just(item1, item2));

        StepVerifier.create(itemService.getPaginatedItems(perPage, page))
                .expectNextMatches(item -> item.getId().equals(1) && item.getName().equals("Test Item 1"))
                .expectNextMatches(item -> item.getId().equals(2) && item.getName().equals("Alpha Item 2"))
                .verifyComplete();
                
        verify(itemRepo).findAll(any(Sort.class));
        verify(itemCacheService).getOrCacheItemList(anyString(), any());
    }

    @Test
    void getItemById_whenFound_shouldReturnItem() {
        when(itemRepo.findById(1)).thenReturn(Mono.just(item1));

        StepVerifier.create(itemService.getItemById(1))
                .expectNextMatches(item -> item.getId().equals(1) && item.getName().equals("Test Item 1"))
                .verifyComplete();
                
        verify(itemRepo).findById(1);
        verify(itemCacheService).getOrCacheItemDetail(eq(1), any());
    }

    @Test
    void getItemById_whenNotFound_shouldReturnEmptyMono() {
        when(itemRepo.findById(99)).thenReturn(Mono.empty());
        
        StepVerifier.create(itemService.getItemById(99))
                .verifyComplete();
                
        verify(itemRepo).findById(99);
        verify(itemCacheService).getOrCacheItemDetail(eq(99), any());
    }

    @Test
    void searchItems_shouldCallRepositoryWithCorrectQuery() {
        String query = "Test";
        SortingCategory sortCategory = SortingCategory.PRICE;
        
        when(itemRepo.findBySearchQuery(query)).thenReturn(Flux.just(item1));

        StepVerifier.create(itemService.searchItems(query, sortCategory))
                .expectNextMatches(item -> item.getId().equals(1) && item.getName().equals("Test Item 1"))
                .verifyComplete();
                
        verify(itemRepo).findBySearchQuery(query);
        verify(itemCacheService).getOrCacheItemList(anyString(), any());
    }

    @Test
    void createItem_shouldSaveItem() {
        Item newItem = new Item();
        newItem.setName("New");
        newItem.setId(3);

        when(itemRepo.save(newItem)).thenReturn(Mono.just(newItem));

        StepVerifier.create(itemService.createItem(newItem))
                .expectNext(newItem)
                .verifyComplete();
                
        verify(itemRepo).save(newItem);
        verify(itemCacheService).invalidateItemCache(3);
    }

    @Test
    void updateItemAmount_whenItemExistsAndAmountValid_shouldUpdateAndSave() {
        int delta = 5;
        Item updatedItem = new Item();
        updatedItem.setId(1);
        updatedItem.setAmount(10);
        
        when(itemRepo.findById(1)).thenReturn(Mono.just(item1));
        when(itemRepo.save(any(Item.class))).thenReturn(Mono.just(updatedItem));

        StepVerifier.create(itemService.updateItemAmount(1, delta))
                .expectNext(updatedItem)
                .verifyComplete();
                
        verify(itemRepo).findById(1);
        verify(itemRepo).save(any(Item.class));
        verify(itemCacheService).invalidateItemCache(1);
    }

    @Test
    void updateItemAmount_whenResultingAmountNegative_shouldReturnError() {
        int delta = -10;
        
        when(itemRepo.findById(1)).thenReturn(Mono.just(item1));

        StepVerifier.create(itemService.updateItemAmount(1, delta))
                .expectError(IllegalStateException.class)
                .verify();
                
        verify(itemRepo).findById(1);
        verify(itemRepo, never()).save(any(Item.class));
        verify(itemCacheService, never()).invalidateItemCache(anyInt());
    }

    @Test
    void updateItemAmount_whenItemNotFound_shouldReturnError() {
        int delta = 5;
        
        when(itemRepo.findById(99)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.updateItemAmount(99, delta))
                .expectError(RuntimeException.class)
                .verify();
                
        verify(itemRepo).findById(99);
        verify(itemRepo, never()).save(any(Item.class));
        verify(itemCacheService, never()).invalidateItemCache(anyInt());
    }

    @Test
    void getTotalItemsCount_shouldReturnCountFromRepository() {
        long expectedCount = 42L;
        
        when(itemRepo.count()).thenReturn(Mono.just(expectedCount));

        StepVerifier.create(itemService.getTotalItemsCount())
                .expectNext(expectedCount)
                .verifyComplete();
                
        verify(itemRepo).count();
    }
}