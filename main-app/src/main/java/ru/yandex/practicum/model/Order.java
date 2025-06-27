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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Column("user_id")
    Integer userId;
    @Column("created_at")
    LocalDateTime createdAt = LocalDateTime.now();
    @Transient
    List<OrderItem> orderItems;

    public String getTotalPriceFormatted() {
        return Formatter.DECIMAL_FORMAT.format(totalSum);
    }
    
    public String getTotalSumFormatted() {
        return Formatter.DECIMAL_FORMAT.format(totalSum);
    }
    
    public String getStatus() {
        return paid ? "PAID" : "NEW";
    }
    
    public String getStatusText() {
        return paid ? "Оплачен" : "Не оплачен";
    }
    
    public String getCreatedAt() {
        if (createdAt == null) {
            return "Дата не указана";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return createdAt.format(formatter);
    }

}