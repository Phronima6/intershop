package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.ImageRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceTest {

    @Mock ImageRepository imageRepository;
    @InjectMocks ImageService imageService;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testGetImage() {
        when(imageRepository.findById(anyInt())).thenReturn(Mono.empty());
        assertNotNull(imageService.getImage(1).block());
    }

} 