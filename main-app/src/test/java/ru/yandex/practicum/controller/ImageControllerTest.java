package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.ImageService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageControllerTest {

    @Mock ImageService imageService;
    @InjectMocks ImageController imageController;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testGetImage() {
        when(imageService.getImage(anyInt())).thenReturn(Mono.just(new byte[0]));
        assertNotNull(imageController.getImage(1).block());
    }

} 