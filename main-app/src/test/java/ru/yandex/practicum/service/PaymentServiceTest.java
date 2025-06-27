package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.OrderRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock PaymentClientService paymentClientService;
    @InjectMocks PaymentService paymentService;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testProcessPaymentForOrder() {
        when(orderRepository.findById(anyInt())).thenReturn(Mono.empty());
        assertNotNull(paymentService.processPaymentForOrder(1, "user").blockOptional());
    }

} 