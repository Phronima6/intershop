package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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

    public Flux<Item> getPaginatedItems(int perPage, int page) {
        return itemRepo.findAll(Sort.by("id"))
                .skip((long) (page - 1) * perPage)
                .take(perPage);
    }

    public Mono<Item> getItemById(int id) {
        return itemRepo.findById(id);
    }

    public Flux<Item> searchItems(String query, SortingCategory sort) {
        return itemRepo.findBySearchQuery(query)
                .sort((i1, i2) -> {
                    return switch (sort) {
                        case ALPHA -> i1.getName().compareTo(i2.getName());
                        case PRICE -> i1.getPrice().compareTo(i2.getPrice());
                        default -> i1.getId().compareTo(i2.getId());
                    };
                });
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
                        return itemRepo.save(item);
                    });
        } else {
            return itemRepo.save(item);
        }
    }

    public Mono<Item> updateItemAmount(int id, int delta) {
        return itemRepo.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Item not found")))
                .flatMap(item -> {
                    int newAmount = item.getAmount() + delta;
                    if (newAmount < 0) {
                        return Mono.error(new IllegalStateException("Количество товара не может быть отрицательным"));
                    }
                    item.setAmount(newAmount);
                    return itemRepo.save(item);
                });
    }

    public Mono<Long> getTotalItemsCount() {
        return itemRepo.count();
    }
}