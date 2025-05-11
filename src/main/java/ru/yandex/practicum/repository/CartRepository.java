package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.model.CartItem;
import reactor.core.publisher.Flux;

public interface CartRepository extends R2dbcRepository<CartItem, Integer> {

    Flux<CartItem> findByItemId(Integer itemId);

}