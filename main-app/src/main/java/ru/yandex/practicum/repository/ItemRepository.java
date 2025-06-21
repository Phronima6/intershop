package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Item;
import reactor.core.publisher.Flux;

public interface ItemRepository extends R2dbcRepository<Item, Integer> {

    @Query("SELECT * FROM items WHERE LOWER(name) LIKE LOWER(CONCAT('%',:query,'%')) " +
           "OR LOWER(description) LIKE LOWER(CONCAT('%',:query,'%'))")
    Flux<Item> findBySearchQuery(@Param("query") String query);
}