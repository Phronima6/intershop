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
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.ItemRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepo;
    
    @Mock
    private ImageRepository imageRepo;

    @InjectMocks
    private ItemService itemService;

    private Item item1;
    private Item item2;

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
    }

    @Test
    void getPaginatedItems_shouldReturnContentFromRepository() {
        int page = 1;
        int perPage = 10;
        
        when(itemRepo.findAll(any(Sort.class))).thenReturn(Flux.just(item1, item2));

        StepVerifier.create(itemService.getPaginatedItems(perPage, page))
                .expectNext(item1)
                .expectNext(item2)
                .verifyComplete();
                
        verify(itemRepo).findAll(any(Sort.class));
    }

    @Test
    void getItemById_whenFound_shouldReturnItem() {
        when(itemRepo.findById(1)).thenReturn(Mono.just(item1));
        
        StepVerifier.create(itemService.getItemById(1))
                .expectNext(item1)
                .verifyComplete();
                
        verify(itemRepo).findById(1);
    }

    @Test
    void getItemById_whenNotFound_shouldReturnEmptyMono() {
        when(itemRepo.findById(99)).thenReturn(Mono.empty());
        
        StepVerifier.create(itemService.getItemById(99))
                .verifyComplete();
                
        verify(itemRepo).findById(99);
    }

    @Test
    void searchItems_shouldCallRepositoryWithCorrectQuery() {
        String query = "Test";
        SortingCategory sortCategory = SortingCategory.PRICE;
        
        when(itemRepo.findBySearchQuery(query)).thenReturn(Flux.just(item1));

        StepVerifier.create(itemService.searchItems(query, sortCategory))
                .expectNext(item1)
                .verifyComplete();
                
        verify(itemRepo).findBySearchQuery(query);
    }

    @Test
    void createItem_shouldSaveItem() {
        Item newItem = new Item();
        newItem.setName("New");

        when(itemRepo.save(newItem)).thenReturn(Mono.just(item1));

        StepVerifier.create(itemService.createItem(newItem))
                .expectNext(item1)
                .verifyComplete();
                
        verify(itemRepo).save(newItem);
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