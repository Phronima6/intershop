package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import ru.yandex.practicum.model.CartItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartRepository extends R2dbcRepository<CartItem, Integer> {

    Flux<CartItem> findByItemId(Integer itemId);
    
    Flux<CartItem> findByUserId(Integer userId);
    
    Flux<CartItem> findByItemIdAndUserId(Integer itemId, Integer userId);
    
    Mono<CartItem> findByIdAndUserId(Integer id, Integer userId);
    
    Mono<Boolean> existsByIdAndUserId(Integer id, Integer userId);
    
    @Modifying
    @Query("DELETE FROM cart_items WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Integer userId);

}