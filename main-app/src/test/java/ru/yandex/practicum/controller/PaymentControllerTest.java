package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.OrderService;
import ru.yandex.practicum.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock OrderService orderService;
    @Mock PaymentService paymentService;
    @InjectMocks PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testShowPaymentPage() {
        when(orderService.getOrder(anyInt())).thenReturn(Mono.just(new ru.yandex.practicum.model.Order()));
        when(orderService.getUserBalanceFormatted()).thenReturn(Mono.just("0"));
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        assertNotNull(paymentController.showPaymentPage(1, exchange).block());
    }

    @Test
    void testProcessPayment() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("orderId", "1");
        when(exchange.getFormData()).thenReturn(Mono.just(formData));
        Authentication principal = mock(Authentication.class);
        when(principal.getName()).thenReturn("testuser");
        when(exchange.getPrincipal()).thenReturn(Mono.just(principal));
        when(paymentService.processPaymentForOrder(anyInt(), anyString())).thenReturn(Mono.just(true));
        assertNotNull(paymentController.processPayment(exchange).block());
    }

} 