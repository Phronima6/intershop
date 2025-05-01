package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.model.Image;
import ru.yandex.practicum.repository.ImageRepository;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ImageServiceTest {

    @Mock
    ImageRepository imageRepository;
    @InjectMocks
    ImageService imageService;
    Image image;
    byte[] imageBytes;

    @BeforeEach
    void setUp() {
        imageBytes = "test data".getBytes();
        image = new Image();
        image.setId(1);
        image.setImageBytes(imageBytes);
    }

    @Test
    void getImage_whenImageExists_shouldReturnBytes() {
        int imageId = 1;
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        byte[] result = imageService.getImage(imageId);
        assertArrayEquals(imageBytes, result);
        verify(imageRepository).findById(imageId);
    }

    @Test
    void getImage_whenImageNotFound_shouldThrowException() {
        int imageId = 99;
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            imageService.getImage(imageId);
        });
        verify(imageRepository).findById(imageId);
    }

}