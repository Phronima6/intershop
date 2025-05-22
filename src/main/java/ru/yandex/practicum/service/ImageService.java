package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.ImageRepository;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
@RequiredArgsConstructor
public class ImageService {

    ImageRepository imageRepository;

    public Mono<byte[]> getImage(int imageId) {
        return imageRepository.findById(imageId)
                .switchIfEmpty(Mono.error(new RuntimeException("Image not found for id: " + imageId)))
                .map(image -> image.getImageBytes());
    }
}