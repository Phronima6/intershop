package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.cache.ItemDetailCache;
import ru.yandex.practicum.cache.ItemListCache;
import ru.yandex.practicum.repository.ItemRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Item;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyLong;

class ItemCacheServiceTest {

    @Mock ItemListCache itemListCache;
    @Mock ItemDetailCache itemDetailCache;
    @Mock ItemRepository itemRepository;
    @Mock ReactiveRedisTemplate<String, ItemDetailCache> itemDetailRedisTemplate;
    @Mock ReactiveRedisTemplate<String, ItemListCache> itemListRedisTemplate;
    @Mock ReactiveValueOperations<String, ItemDetailCache> valueOps;
    @Mock ReactiveListOperations<String, ItemListCache> listOps;
    @InjectMocks ItemCacheService itemCacheService;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        when(itemDetailRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(itemListRedisTemplate.opsForList()).thenReturn(listOps);
        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        when(listOps.range(anyString(), anyLong(), anyLong())).thenReturn(Flux.empty());
        itemCacheService = new ItemCacheService(itemDetailRedisTemplate, itemListRedisTemplate);
    }

    @Test void testGetOrCacheItemDetail() {
        Mono<Item> itemLoader = Mono.empty();
        assertNotNull(itemCacheService.getOrCacheItemDetail(1, itemLoader));
    }

    @Test void testGetOrCacheItemList() {
        Flux<Item> itemListLoader = Flux.empty();
        assertNotNull(itemCacheService.getOrCacheItemList("test", itemListLoader));
    }

} 