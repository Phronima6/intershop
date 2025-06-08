package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Image;
import ru.yandex.practicum.repository.ImageRepository;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
@RequiredArgsConstructor
public class ImageService {

    ImageRepository imageRepository;

    public Mono<byte[]> getImage(int imageId) {
        return imageRepository.findById(imageId)
                .map(Image::getImageBytes)
                .switchIfEmpty(Mono.just(new byte[0]))
                .onErrorResume(e -> {
                    log.error("Error retrieving image: {}", e.getMessage());
                    return Mono.just(new byte[0]);
                });
    }
}