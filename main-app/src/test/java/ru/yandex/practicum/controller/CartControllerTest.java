package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.service.CartService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartControllerTest {

    @Mock CartService cartService;
    @Mock ItemRepository itemRepository;
    @Mock ServerWebExchange exchange;
    @InjectMocks CartController cartController;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testViewCart() {
        when(cartService.getCartItems()).thenReturn(Flux.empty());
        when(cartService.getFormattedTotalPrice()).thenReturn(Mono.just("0"));
        assertNotNull(cartController.viewCart().block());
    }

    @Test void testAddToCart() {
        when(itemRepository.findById(anyInt())).thenReturn(Mono.empty());
        assertNotNull(cartController.addToCart(1, 1, exchange).block());
    }

} 