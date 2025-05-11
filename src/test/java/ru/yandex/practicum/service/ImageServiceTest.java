package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.model.Image;
import ru.yandex.practicum.repository.ImageRepository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    private Image image1;
    private byte[] imageBytes;

    @BeforeEach
    void setUp() {
        imageBytes = "test data".getBytes();
        image1 = new Image();
        image1.setId(1);
        image1.setImageBytes(imageBytes);
    }

    @Test
    void getImage_whenImageExists_shouldReturnBytes() {
        int imageId = 1;
        when(imageRepository.findById(imageId)).thenReturn(Mono.just(image1));

        StepVerifier.create(imageService.getImage(imageId))
                .expectNext(imageBytes)
                .verifyComplete();

        verify(imageRepository).findById(imageId);
    }

    @Test
    void getImage_whenImageNotFound_shouldReturnError() {
        int imageId = 99;
        when(imageRepository.findById(imageId)).thenReturn(Mono.empty());

        StepVerifier.create(imageService.getImage(imageId))
                .expectError(RuntimeException.class)
                .verify();

        verify(imageRepository).findById(imageId);
    }
}