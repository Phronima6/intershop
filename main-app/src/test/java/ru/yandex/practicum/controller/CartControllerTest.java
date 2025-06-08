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
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.service.CartService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private CartService cartService;

    private CartItem cartItem1;
    private Item item1;

    @TestConfiguration
    static class ControllerTestConfiguration {
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
        Mockito.reset(cartService);
        item1 = new Item();
        item1.setId(1);
        item1.setName("Test Item 1");
        item1.setPrice(10);

        cartItem1 = new CartItem();
        cartItem1.setId(101);
        cartItem1.setItemId(1);
        cartItem1.setItem(item1);
        cartItem1.setQuantity(2);
    }

    @Test
    void increaseQuantity_shouldCallServiceAndRedirect() {
        int cartItemId = 101;
        given(cartService.increaseCartItemQuantity(cartItemId)).willReturn(Mono.empty());

        webClient.post().uri("/cart/item/{cartItemId}/plus", cartItemId)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(cartService).increaseCartItemQuantity(cartItemId);
    }

    @Test
    void decreaseQuantity_shouldCallServiceAndRedirect() {
        int cartItemId = 101;
        given(cartService.decreaseCartItemQuantity(cartItemId)).willReturn(Mono.empty());

        webClient.post().uri("/cart/item/{cartItemId}/minus", cartItemId)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(cartService).decreaseCartItemQuantity(cartItemId);
    }

    @Test
    void removeCartItemById_shouldCallServiceAndRedirect() {
        int cartItemId = 101;
        PageNames redirectTo = PageNames.CART;
        given(cartService.removeCartItemById(cartItemId)).willReturn(Mono.empty());

        webClient.post().uri(uriBuilder -> uriBuilder
                        .path("/cart/item/{cartItemId}/remove")
                        .queryParam("redirectTo", redirectTo.name())
                        .build(cartItemId))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(cartService).removeCartItemById(cartItemId);
    }
    
    @Test
    void removeCartItemById_shouldRedirectToItemsWhenSpecified() {
        int cartItemId = 101;
        PageNames redirectTo = PageNames.MAIN;
        given(cartService.removeCartItemById(cartItemId)).willReturn(Mono.empty());

        webClient.post().uri(uriBuilder -> uriBuilder
                        .path("/cart/item/{cartItemId}/remove")
                        .queryParam("redirectTo", redirectTo.name())
                        .build(cartItemId))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/main/items");

        verify(cartService).removeCartItemById(cartItemId);
    }
}