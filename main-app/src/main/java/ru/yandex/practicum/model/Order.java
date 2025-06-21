package ru.yandex.practicum.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.yandex.practicum.util.Formatter;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("orders")
public class Order {

    @Id
    int id;
    
    @Column("total_sum")
    double totalSum;

    @Column("paid")
    boolean paid = false;
    
    @Transient
    List<OrderItem> orderItems;

    public String getTotalSumFormatted() {
        return Formatter.DECIMAL_FORMAT.format(totalSum);
    }
    
    public String getStatusText() {
        return paid ? "Оплачен" : "Не оплачен";
    }
}