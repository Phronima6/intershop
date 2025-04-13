package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}