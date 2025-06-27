package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.cache.ItemDetailCache;
import ru.yandex.practicum.cache.ItemListCache;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.ImageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepo;
    private final ImageRepository imageRepo;
    private final ItemCacheService itemCacheService;

    public Flux<Item> getPaginatedItems(int perPage, int page) {
        String cacheKey = "page_" + page + "_size_" + perPage;
        return itemCacheService.getOrCacheItemList(cacheKey, 
                itemRepo.findAll(Sort.by("id"))
                        .skip((long) (page - 1) * perPage)
                        .take(perPage))
                .flatMap(this::convertItemListCacheToItem);
    }

    public Mono<Item> getItemById(int id) {
        return itemCacheService.getOrCacheItemDetail(id, itemRepo.findById(id))
                .flatMap(this::convertItemDetailCacheToItem);
    }

    public Flux<Item> searchItems(String query, SortingCategory sort) {
        String cacheKey = "search_" + query + "_sort_" + sort.name();
        return itemCacheService.getOrCacheItemList(cacheKey, 
                itemRepo.findBySearchQuery(query)
                        .sort((i1, i2) -> {
                            return switch (sort) {
                                case ALPHA -> i1.getName().compareTo(i2.getName());
                                case PRICE -> i1.getPrice().compareTo(i2.getPrice());
                                default -> i1.getId().compareTo(i2.getId());
                            };
                        }))
                .flatMap(this::convertItemListCacheToItem);
    }

    public Mono<Item> createItem(Item item) {
        if (item.getImageFile() != null) {
            return item.getImageFile().content()
                    .reduce(new byte[0], (prev, buffer) -> {
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        return bytes;
                    })
                    .flatMap(bytes -> {
                        var image = new ru.yandex.practicum.model.Image();
                        image.setImageBytes(bytes);
                        return imageRepo.save(image);
                    })
                    .flatMap(savedImage -> {
                        item.setImageId(savedImage.getId());
                        return itemRepo.save(item)
                                .doOnNext(savedItem -> invalidateItemCache(savedItem.getId()));
                    });
        } else {
            return itemRepo.save(item)
                    .doOnNext(savedItem -> invalidateItemCache(savedItem.getId()));
        }
    }

    public Mono<Item> updateItemAmount(int id, int delta) {
        return itemRepo.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Попытка обновить несуществующий товар с ID: {}", id);
                    return Mono.error(new RuntimeException("Item not found"));
                }).cast(Item.class))
                .flatMap(item -> {
                    int newAmount = item.getAmount() + delta;
                    if (newAmount < 0) {
                        return Mono.error(new IllegalStateException("Количество товара не может быть отрицательным"));
                    }
                    item.setAmount(newAmount);
                    return itemRepo.save(item)
                            .doOnNext(savedItem -> invalidateItemCache(savedItem.getId()));
                });
    }

    public Mono<Long> getTotalItemsCount() {
        return itemRepo.count();
    }
    
    private void invalidateItemCache(Integer itemId) {
        if (itemId == null) return;
        itemCacheService.invalidateItemCache(itemId)
                .subscribe(
                        null,
                        error -> log.error("Ошибка при инвалидации кеша товара: {}", error.getMessage()),
                        () -> log.debug("Кеш товара с ID {} успешно инвалидирован", itemId)
                );
    }

    private Mono<Item> convertItemDetailCacheToItem(ItemDetailCache cachedItem) {
        Item item = new Item();
        item.setId(cachedItem.getId());
        item.setName(cachedItem.getName());
        item.setDescription(cachedItem.getDescription());
        item.setPrice((int) Math.round(cachedItem.getPrice()));
        item.setImageId(cachedItem.getImageId());
        return Mono.just(item);
    }

    private Mono<Item> convertItemListCacheToItem(ItemListCache cachedItem) {
        Item item = new Item();
        item.setId(cachedItem.getId());
        item.setName(cachedItem.getName());
        item.setDescription(cachedItem.getDescription());
        item.setPrice((int) Math.round(cachedItem.getPrice()));
        return Mono.just(item);
    }

}