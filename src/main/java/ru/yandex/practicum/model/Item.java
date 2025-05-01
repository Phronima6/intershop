package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.util.Formatter;
import java.io.IOException;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "items")
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false)
    String name;
    @Column(columnDefinition = "TEXT")
    String description;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_id")
    Image image;
    @Column(nullable = false, precision = 10, scale = 2)
    Integer price;
    @Column(nullable = false)
    @Builder.Default
    Integer amount = 0;
    @Transient
    @ToString.Exclude
    MultipartFile imageFile;

    public String getPriceFormatted() {
        return Formatter.DECIMAL_FORMAT.format(price);
    }

    public void processImage() throws IOException {
        if (this.imageFile != null && !this.imageFile.isEmpty()) {
            if (this.image == null) {
                this.image = new Image();
            }
            this.image.setImageBytes(this.imageFile.getBytes());
        }
    }

}