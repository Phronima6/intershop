package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cache.ItemDetailCache;
import ru.yandex.practicum.cache.ItemListCache;
import ru.yandex.practicum.model.Item;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ItemCacheService {

    final ReactiveRedisTemplate<String, ItemDetailCache> itemDetailRedisTemplate;
    final ReactiveRedisTemplate<String, ItemListCache> itemListRedisTemplate;
    
    @Value("${cache.item.ttl:3600}")
    long itemCacheTtlSeconds;
    
    @Value("${cache.item.detail-prefix:item:detail:}")
    String detailPrefix;
    
    @Value("${cache.item.list-prefix:item:list:}")
    String listPrefix;
    
    @Value("${cache.item.search-prefix:item:search:}")
    String searchPrefix;

    public Mono<ItemDetailCache> getOrCacheItemDetail(int itemId, Mono<Item> itemLoader) {
        String key = detailPrefix + itemId;
        return itemDetailRedisTemplate.opsForValue().get(key)
                .doOnNext(cached -> log.debug("Найден в кеше детальный товар с ID: {}", itemId))
                .switchIfEmpty(
                    Mono.defer(() -> {
                        log.debug("Кеш-мисс для детального товара с ID: {}", itemId);
                        return itemLoader
                                .map(this::mapToItemDetailCache)
                                .flatMap(itemDetail -> cacheItemDetail(key, itemDetail));
                    })
                );
    }

    private Mono<ItemDetailCache> cacheItemDetail(String key, ItemDetailCache itemDetail) {
        log.debug("Кеширование детального товара с ID: {}", itemDetail.getId());
        return itemDetailRedisTemplate.opsForValue()
                .set(key, itemDetail, Duration.ofSeconds(itemCacheTtlSeconds))
                .thenReturn(itemDetail);
    }

    public Flux<ItemListCache> getOrCacheItemList(String searchKey, Flux<Item> itemListLoader) {
        String key = searchPrefix + searchKey;
        return itemListRedisTemplate.opsForList().range(key, 0, -1)
                .doOnNext(cached -> log.debug("Найден в кеше список товаров по ключу: {}", searchKey))
                .switchIfEmpty(
                    itemListLoader
                            .map(this::mapToItemListCache)
                            .collectList()
                            .flatMapMany(itemList -> {
                                log.debug("Кеш-мисс для списка товаров по ключу: {}", searchKey);
                                if (itemList.isEmpty()) {
                                    return Flux.empty();
                                }
                                return cacheItemList(key, Flux.fromIterable(itemList));
                            })
                );
    }

    private Flux<ItemListCache> cacheItemList(String key, Flux<ItemListCache> itemList) {
        log.debug("Кеширование списка товаров по ключу: {}", key);
        return itemList.collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.empty();
                    }
                    return itemListRedisTemplate.delete(key)
                            .then(itemListRedisTemplate.opsForList().rightPushAll(key, list))
                            .then(itemListRedisTemplate.expire(key, Duration.ofSeconds(itemCacheTtlSeconds)))
                            .thenReturn(list);
                })
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Void> invalidateItemCache(int itemId) {
        String detailKey = detailPrefix + itemId;
        log.debug("Инвалидация кеша для товара с ID: {}", itemId);
        return itemDetailRedisTemplate.delete(detailKey)
                .then();
    }

    public Mono<Void> invalidateAllItemCache() {
        log.info("Инвалидация всего кеша товаров");
        return itemDetailRedisTemplate.keys(detailPrefix + "*")
                .concatWith(itemListRedisTemplate.keys(listPrefix + "*"))
                .concatWith(itemListRedisTemplate.keys(searchPrefix + "*"))
                .flatMap(key -> itemDetailRedisTemplate.delete(key))
                .then();
    }

    private ItemDetailCache mapToItemDetailCache(Item item) {
        return ItemDetailCache.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageId(item.getImageId())
                .build();
    }

    private ItemListCache mapToItemListCache(Item item) {
        return ItemListCache.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .build();
    }

}