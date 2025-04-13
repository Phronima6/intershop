package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.model.Item;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepo;

    public List<Item> getPaginatedItems(int perPage, int page) {
        return itemRepo.findAll(PageRequest.of(page - 1, perPage, Sort.by("id")))
                .getContent();
    }

    public Optional<Item> getItemById(int id) {
        return itemRepo.findById(id);
    }

    public List<Item> searchItems(String query, SortingCategory sort) {
        Sort sorting = switch (sort) {
            case ALPHA -> Sort.by("name");
            case PRICE -> Sort.by("price");
            default -> Sort.by("id");
        };
        return itemRepo.findBySearchQuery(query, sorting);
    }

    public Item createItem(Item item) throws IOException {
        item.processImage();
        return itemRepo.save(item);
    }

    @Transactional
    public Item updateItemAmount(int id, int delta) {
        Item item = itemRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден."));
        int newAmount = item.getAmount() + delta;
        if (newAmount < 0) {
            throw new IllegalStateException("Количество не может быть отрицательным.");
        }
        item.setAmount(newAmount);
        return itemRepo.save(item);
    }

    public long getTotalItemsCount() {
        return itemRepo.count();
    }

}