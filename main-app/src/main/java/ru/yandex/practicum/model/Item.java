package ru.yandex.practicum.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.http.codec.multipart.FilePart;
import ru.yandex.practicum.util.Formatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table("items")
@Builder
public class Item {

    @Id
    Integer id;
    
    @Column("name")
    String name;
    
    @Column("description")
    String description;
    
    @Column("image_id")
    Integer imageId;
    
    @Column("price")
    Integer price;
    
    @Column("amount")
    @Builder.Default
    Integer amount = 0;
    
    @Transient
    Image image;
    
    @Transient
    @ToString.Exclude
    FilePart imageFile;

    public String getPriceFormatted() {
        return Formatter.DECIMAL_FORMAT.format(price);
    }

    public void processImage() {
    }

}