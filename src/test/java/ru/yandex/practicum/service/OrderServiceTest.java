package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    CartRepository cartRepository;
    @InjectMocks
    OrderService orderService;
    @Captor
    ArgumentCaptor<Order> orderCaptor;
    @Captor
    ArgumentCaptor<OrderItem> orderItemCaptor;
    Item item1;
    Item item2;
    CartItem cartItem1;
    CartItem cartItem2;

    @BeforeEach
    void setUp() {
        item1 = new Item();
        item1.setId(1);
        item1.setPrice(10);
        item1.setAmount(5);
        item2 = new Item();
        item2.setId(2);
        item2.setPrice(20);
        item2.setAmount(3);
        cartItem1 = new CartItem(101, item1, 2); // quantity = 2
        cartItem2 = new CartItem(102, item2, 1); // quantity = 1
    }

    @Test
    void createOrder_whenCartIsEmpty_shouldReturnNull() {
        when(cartRepository.findAll()).thenReturn(Collections.emptyList());
        Order result = orderService.createOrder();
        assertNull(result);
        verify(cartRepository).findAll();
        verify(orderRepository, never()).save(any());
        verify(orderItemRepository, never()).save(any());
        verify(cartRepository, never()).deleteAll();
    }

    @Test
    void createOrder_whenCartHasOnlyZeroAmountItems_shouldReturnNull() {
        item1.setAmount(0);
        cartItem1.setItem(item1);
        when(cartRepository.findAll()).thenReturn(List.of(cartItem1));
        Order result = orderService.createOrder();
        assertNull(result);
        verify(cartRepository).findAll();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_whenCartHasItems_shouldCreateOrderAndOrderItems() {
        List<CartItem> cartItems = List.of(cartItem1, cartItem2);
        Order initialOrder = new Order();
        Order savedOrderWithId = new Order();
        savedOrderWithId.setId(1);
        when(cartRepository.findAll()).thenReturn(cartItems);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrderWithId);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));
        Order result = orderService.createOrder();
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(cartRepository).findAll();
        verify(orderRepository, times(2)).save(orderCaptor.capture());
        verify(orderItemRepository, times(2)).save(orderItemCaptor.capture());
        verify(cartRepository).deleteAll();
        List<OrderItem> capturedOrderItems = orderItemCaptor.getAllValues();
        assertEquals(2, capturedOrderItems.size());
        assertEquals(savedOrderWithId, capturedOrderItems.get(0).getOrder());
        assertEquals(item1, capturedOrderItems.get(0).getItem());
        assertEquals(item1.getAmount(), capturedOrderItems.get(0).getItemAmount());
        assertEquals(savedOrderWithId, capturedOrderItems.get(1).getOrder());
        assertEquals(item2, capturedOrderItems.get(1).getItem());
        assertEquals(item2.getAmount(), capturedOrderItems.get(1).getItemAmount());
        Order finalOrder = orderCaptor.getAllValues().get(1);
        assertNotNull(finalOrder.getOrderItems());
        assertEquals(2, finalOrder.getOrderItems().size());
        assertEquals(110.0, finalOrder.getTotalSum(), 0.001);
    }


    @Test
    void getOrders_shouldReturnListFromRepository() {
        List<Order> expectedOrders = List.of(new Order());
        when(orderRepository.findAll()).thenReturn(expectedOrders);
        List<Order> result = orderService.getOrders();
        assertEquals(expectedOrders, result);
        verify(orderRepository).findAll();
    }

    @Test
    void getOrder_whenFound_shouldReturnOrder() {
        int orderId = 1;
        Order expectedOrder = new Order();
        expectedOrder.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(expectedOrder));
        Order result = orderService.getOrder(orderId);
        assertEquals(expectedOrder, result);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrder_whenNotFound_shouldThrowException() {
        int orderId = 99;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> {
            orderService.getOrder(orderId);
        });
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrdersTotalSum_shouldReturnSumFromRepository() {
        Double expectedSum = 123.45;
        when(orderRepository.getSumOfAllOrders()).thenReturn(expectedSum);
        Double result = orderService.getOrdersTotalSum();
        assertEquals(expectedSum, result);
        verify(orderRepository).getSumOfAllOrders();
    }

    @Test
    void getOrdersTotalSum_whenRepositoryReturnsNull_shouldReturnNull() {
        when(orderRepository.getSumOfAllOrders()).thenReturn(null);
        Double result = orderService.getOrdersTotalSum();
        assertNull(result);
        verify(orderRepository).getSumOfAllOrders();
    }

}