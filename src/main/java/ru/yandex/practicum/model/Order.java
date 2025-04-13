package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.util.Formatter;
import java.util.List;

@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @OneToMany
    @JoinColumn(name = "order_id")
    List<OrderItem> orderItems;
    @Column(name = "total_sum")
    double totalSum;

    public String getTotalSumFormatted() {
        return Formatter.DECIMAL_FORMAT.format(totalSum);
    }

}