package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.CartItem;
import java.util.List;

public interface CartRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByItemId(Integer itemId);

}