package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CartRepository cartRepository;
    @Mock PaymentClientService paymentClientService;
    @Mock UserRepository userRepository;
    @Mock ItemRepository itemRepository;
    @InjectMocks OrderService orderService;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testGetOrders() {
        when(orderRepository.findAll()).thenReturn(Flux.empty());
        assertNotNull(orderService.getOrders().collectList().block());
    }

    @Test void testGetOrder() {
        when(orderRepository.findById(anyInt())).thenReturn(Mono.empty());
        assertNotNull(orderService.getOrder(1).blockOptional());
    }

    @Test
    void testCreateOrderEmptyCart() {
        when(cartRepository.findByUserId(anyInt())).thenReturn(Flux.empty());
        Mono<Order> orderMono = orderService.createOrder();
        assertNotNull(orderMono);
    }

} 