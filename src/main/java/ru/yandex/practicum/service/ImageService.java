package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.repository.ImageRepository;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
@RequiredArgsConstructor
public class ImageService {

    ImageRepository imageRepository;

    public byte[] getImage(int itemId) {
        return imageRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Изображение не найдено для товара с id: " + itemId))
                .getImageBytes();
    }

}