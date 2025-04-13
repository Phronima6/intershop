package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.repository.ItemRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceTest {

    @Mock
    ItemRepository itemRepo;
    @InjectMocks
    ItemService itemService;
    Item item1;
    Item item2;

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
        Pageable pageable = PageRequest.of(page - 1, perPage, Sort.by("id"));
        List<Item> items = List.of(item1, item2);
        Page<Item> itemPage = new PageImpl<>(items, pageable, items.size());
        when(itemRepo.findAll(pageable)).thenReturn(itemPage);
        List<Item> result = itemService.getPaginatedItems(perPage, page);
        assertEquals(items, result);
        verify(itemRepo).findAll(pageable);
    }

    @Test
    void getItemById_whenFound_shouldReturnOptionalWithItem() {
        when(itemRepo.findById(1)).thenReturn(Optional.of(item1));
        Optional<Item> result = itemService.getItemById(1);
        assertTrue(result.isPresent());
        assertEquals(item1, result.get());
        verify(itemRepo).findById(1);
    }

    @Test
    void getItemById_whenNotFound_shouldReturnEmptyOptional() {
        when(itemRepo.findById(99)).thenReturn(Optional.empty());
        Optional<Item> result = itemService.getItemById(99);
        assertTrue(result.isEmpty());
        verify(itemRepo).findById(99);
    }

    @Test
    void searchItems_shouldCallRepositoryWithCorrectQueryAndSort() {
        String query = "Test";
        SortingCategory sortCategory = SortingCategory.PRICE;
        Sort expectedSort = Sort.by("price");
        List<Item> expectedItems = List.of(item1);
        when(itemRepo.findBySearchQuery(query, expectedSort)).thenReturn(expectedItems);
        List<Item> result = itemService.searchItems(query, sortCategory);
        assertEquals(expectedItems, result);
        verify(itemRepo).findBySearchQuery(query, expectedSort);
    }

    @Test
    void createItem_shouldCallProcessImageAndSave() throws IOException {
        Item newItem = new Item();
        newItem.setName("New");
        when(itemRepo.save(newItem)).thenReturn(item1);
        Item result = itemService.createItem(newItem);
        assertEquals(item1, result);
        verify(itemRepo).save(newItem);
    }


    @Test
    void updateItemAmount_whenItemExistsAndAmountValid_shouldUpdateAndSave() {
        int delta = 5;
        when(itemRepo.findById(1)).thenReturn(Optional.of(item1));
        when(itemRepo.save(item1)).thenReturn(item1);
        Item result = itemService.updateItemAmount(1, delta);
        assertEquals(10, result.getAmount());
        verify(itemRepo).findById(1);
        verify(itemRepo).save(item1);
    }

    @Test
    void updateItemAmount_whenResultingAmountNegative_shouldThrowException() {
        int delta = -10;
        when(itemRepo.findById(1)).thenReturn(Optional.of(item1));
        assertThrows(IllegalStateException.class, () -> {
            itemService.updateItemAmount(1, delta);
        });
        verify(itemRepo).findById(1);
        verify(itemRepo, never()).save(any());
    }

    @Test
    void updateItemAmount_whenItemNotFound_shouldThrowException() {
        int delta = 5;
        when(itemRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            itemService.updateItemAmount(99, delta);
        });
        verify(itemRepo).findById(99);
        verify(itemRepo, never()).save(any());
    }

    @Test
    void getTotalItemsCount_shouldReturnCountFromRepository() {
        long expectedCount = 42L;
        when(itemRepo.count()).thenReturn(expectedCount);
        long result = itemService.getTotalItemsCount();
        assertEquals(expectedCount, result);
        verify(itemRepo).count();
    }

}