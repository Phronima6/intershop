package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.service.ImageService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebFluxTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private ImageService imageService;

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public ImageService imageService() {
            return Mockito.mock(ImageService.class);
        }
        
        @Bean
        public ItemRepository itemRepository() {
            return Mockito.mock(ItemRepository.class);
        }
        
        @Bean
        public OrderRepository orderRepository() {
            return Mockito.mock(OrderRepository.class);
        }
        
        @Bean
        public OrderItemRepository orderItemRepository() {
            return Mockito.mock(OrderItemRepository.class);
        }
        
        @Bean
        public CartRepository cartRepository() {
            return Mockito.mock(CartRepository.class);
        }
    }

    @Test
    void getImage_shouldReturnImageBytesWhenFound() {
        int imageId = 1;
        byte[] imageBytes = "test image data".getBytes();

        given(imageService.getImage(imageId)).willReturn(Mono.just(imageBytes));

        webClient.get().uri("/{imageId}/image", imageId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class).isEqualTo(imageBytes);

        verify(imageService).getImage(imageId);
    }

    @Test
    void getImage_shouldReturnErrorWhenImageNotFound() {
        int imageId = 2;
        given(imageService.getImage(imageId))
                .willReturn(Mono.error(new RuntimeException("Image not found")));

        webClient.get().uri("/{imageId}/image", imageId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(imageService).getImage(imageId);
    }
}