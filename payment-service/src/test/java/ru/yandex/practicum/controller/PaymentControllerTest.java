package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.PaymentService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock PaymentService paymentService;
    @InjectMocks PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBalance() {
        when(paymentService.getBalance(anyString())).thenReturn(Mono.just(1000.0));
        Mono<Double> result = paymentController.getBalance("testuser");
        assertEquals(1000.0, result.block());
    }

    @Test
    void testInitBalance() {
        when(paymentService.initBalance(anyString())).thenReturn(Mono.empty());
        Mono<Void> result = paymentController.initBalance("testuser");
        assertNull(result.block());
    }

    @Test
    void testDoPayment() {
        when(paymentService.doPayment(anyDouble(), anyString())).thenReturn(Mono.empty());
        Mono<Void> result = paymentController.doPayment(100.0, "testuser");
        assertNull(result.block());
    }

} 