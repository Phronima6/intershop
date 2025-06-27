package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends R2dbcRepository<Order, Integer> {

    @Query("SELECT SUM(total_sum) FROM orders")
    Mono<Double> getSumOfAllOrders();
    
    @Query("SELECT SUM(total_sum) FROM orders WHERE user_id = :userId")
    Mono<Double> getSumOfAllOrdersByUserId(Integer userId);
    
    Flux<Order> findByUserId(Integer userId);
    
    Mono<Order> findByIdAndUserId(Integer id, Integer userId);

}