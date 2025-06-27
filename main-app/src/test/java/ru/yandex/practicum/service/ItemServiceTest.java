package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.ItemRepository;
import org.springframework.data.domain.Sort;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    @Mock ItemRepository itemRepository;
    @Mock ImageRepository imageRepository;
    @Mock ItemCacheService itemCacheService;
    @InjectMocks ItemService itemService;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemService(itemRepository, imageRepository, itemCacheService);
    }

    @Test void testGetPaginatedItems() {
        when(itemRepository.findAll(any(Sort.class))).thenReturn(Flux.empty());
        when(itemCacheService.getOrCacheItemList(anyString(), any())).thenReturn(Flux.empty());
        assertNotNull(itemService.getPaginatedItems(10, 1).collectList().block());
    }

    @Test void testGetItemById() {
        when(itemRepository.findById(anyInt())).thenReturn(Mono.empty());
        when(itemCacheService.getOrCacheItemDetail(anyInt(), any())).thenReturn(Mono.empty());
        assertNotNull(itemService.getItemById(1).blockOptional());
    }

} 