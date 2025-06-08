package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.model.Order;
import reactor.core.publisher.Mono;

public interface OrderRepository extends R2dbcRepository<Order, Integer> {

    @Query("SELECT SUM(total_sum) FROM orders")
    Mono<Double> getSumOfAllOrders();
}