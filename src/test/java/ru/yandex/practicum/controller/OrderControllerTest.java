package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.OrderService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    private Order order1;

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }

        @Bean
        public CartService cartService() {
            return Mockito.mock(CartService.class);
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

    @BeforeEach
    void setUp() {
        Mockito.reset(orderService, cartService);
        order1 = new Order();
        order1.setId(1);
    }

    @Test
    void createOrder_shouldRedirectWhenOrderIsEmpty() {
        given(orderService.createOrder()).willReturn(Mono.empty());

        webClient.post().uri("/create-order")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(orderService).createOrder();
    }

    @Test
    void getOrder_shouldRedirectWhenNotFound() {
        int orderId = 99;
        given(orderService.getOrder(orderId)).willReturn(Mono.empty());

        webClient.get().uri("/orders/{id}", orderId)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders");

        verify(orderService).getOrder(orderId);
    }
}