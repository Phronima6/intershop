package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.OrderService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock OrderService orderService;
    @Mock CartService cartService;
    @Mock ServerWebExchange exchange;
    @InjectMocks OrderController orderController;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testGetOrders() {
        when(orderService.getOrders()).thenReturn(Flux.empty());
        when(orderService.getOrdersTotalSumFormatted()).thenReturn(Mono.just("0"));
        when(orderService.getUserBalanceFormatted()).thenReturn(Mono.just("0"));
        assertNotNull(orderController.getOrders().block());
    }

    @Test void testGetOrder() {
        when(orderService.getOrder(anyInt())).thenReturn(Mono.empty());
        when(orderService.getUserBalanceFormatted()).thenReturn(Mono.just("0"));
        assertNotNull(orderController.getOrder(1).block());
    }

} 