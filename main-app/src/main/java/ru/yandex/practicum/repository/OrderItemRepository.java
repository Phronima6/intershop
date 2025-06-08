package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.model.OrderItem;
import reactor.core.publisher.Flux;

public interface OrderItemRepository extends R2dbcRepository<OrderItem, Integer> {
    
    Flux<OrderItem> findByOrderId(Integer orderId);
}