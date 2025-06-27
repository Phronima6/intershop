package ru.yandex.practicum.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("order_items")
public class OrderItem {

    @Id
    int id;
    @Column("order_id")
    Integer orderId;
    @Column("item_id")
    Integer itemId;
    @Column("item_amount")
    int itemAmount;
    @Transient
    Order order;
    @Transient
    Item item;

}