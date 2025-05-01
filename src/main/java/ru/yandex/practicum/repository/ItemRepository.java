package ru.yandex.practicum.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Item;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    @Query("""
        SELECT i FROM Item i
        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%',:query,'%')) 
        OR LOWER(i.description) LIKE LOWER(CONCAT('%',:query,'%'))
        """)
    List<Item> findBySearchQuery(@Param("query") String query, Sort sort);

}