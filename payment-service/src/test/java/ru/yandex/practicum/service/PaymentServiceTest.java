package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    void testInitBalance() {
        Mono<Void> result = paymentService.initBalance("testuser");
        assertNotNull(result);
    }

    @Test
    void testGetBalance() {
        paymentService.initBalance("testuser").block();
        Mono<Double> balance = paymentService.getBalance("testuser");
        assertNotNull(balance.block());
    }

    @Test
    void testDoPayment() {
        paymentService.initBalance("testuser").block();
        paymentService.doPayment(100, "testuser").block();
        Double balance = paymentService.getBalance("testuser").block();
        assertTrue(balance <= 1000.0);
    }

} 