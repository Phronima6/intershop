package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.ImageService;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ImageController {

    ImageService imageService;

    @GetMapping(value = "/{imageId}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<byte[]> getImage(@PathVariable(name = "imageId") final int imageId) {
        return imageService.getImage(imageId);
    }
}