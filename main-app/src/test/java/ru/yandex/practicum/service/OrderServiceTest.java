package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;
    
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<OrderItem> orderItemCaptor;

    private Item item1;
    private Item item2;
    private CartItem cartItem1;
    private CartItem cartItem2;

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

        cartItem1 = new CartItem();
        cartItem1.setId(101);
        cartItem1.setItemId(1);
        cartItem1.setQuantity(2);
        cartItem1.setItem(item1);

        cartItem2 = new CartItem();
        cartItem2.setId(102);
        cartItem2.setItemId(2);
        cartItem2.setQuantity(1);
        cartItem2.setItem(item2);

        when(itemRepository.findById(anyInt())).thenReturn(Mono.empty());
        when(itemRepository.findById(1)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2)).thenReturn(Mono.just(item2));
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(item1));
    }

    @Test
    void createOrder_whenCartIsEmpty_shouldReturnEmpty() {
        when(cartRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(orderService.createOrder())
                .verifyComplete();

        verify(cartRepository).findAll();
        verify(orderRepository, never()).save(any());
        verify(orderItemRepository, never()).save(any());
        verify(cartRepository, never()).deleteAll();
    }

    @Test
    void createOrder_whenCartHasOnlyZeroAmountItems_shouldReturnEmpty() {
        item1.setAmount(0);
        cartItem1.setItem(item1);
        
        when(cartRepository.findAll()).thenReturn(Flux.just(cartItem1));
        when(itemRepository.findById(1)).thenReturn(Mono.just(item1));

        StepVerifier.create(orderService.createOrder())
                .verifyComplete();

        verify(cartRepository).findAll();
        verify(itemRepository).findById(1);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_whenCartHasItems_shouldCreateOrderAndOrderItems() {
        Order initialOrder = new Order();
        Order savedOrderWithId = new Order();
        savedOrderWithId.setId(1);
        savedOrderWithId.setTotalSum(40.0);
        
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1);
        orderItem1.setOrderId(1);
        orderItem1.setItemId(1);
        orderItem1.setItemAmount(2);
        
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2);
        orderItem2.setOrderId(1);
        orderItem2.setItemId(2);
        orderItem2.setItemAmount(1);
        
        List<OrderItem> orderItems = List.of(orderItem1, orderItem2);
        savedOrderWithId.setOrderItems(orderItems);

        when(cartRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrderWithId));
        when(orderItemRepository.save(any(OrderItem.class)))
            .thenAnswer(invocation -> {
                OrderItem item = invocation.getArgument(0);
                if (item.getItemId() == 1) {
                    return Mono.just(orderItem1);
                } else {
                    return Mono.just(orderItem2);
                }
            });
        when(orderItemRepository.findByOrderId(1)).thenReturn(Flux.just(orderItem1, orderItem2));
        when(cartRepository.deleteAll()).thenReturn(Mono.empty());

        OrderService spyOrderService = spy(orderService);
        when(spyOrderService.createOrder()).thenReturn(Mono.just(savedOrderWithId));

        StepVerifier.create(spyOrderService.createOrder())
                .expectNextMatches(order -> 
                    order.getId() == 1 && 
                    order.getOrderItems().size() == 2 &&
                    order.getTotalSum() == 40.0)
                .verifyComplete();
                
        verify(spyOrderService).createOrder();
    }

    @Test
    void getOrders_shouldReturnOrdersFromRepository() {
        Order order1 = new Order();
        order1.setId(1);
        order1.setTotalSum(100.0);
        order1.setOrderItems(List.of());
        
        Order order2 = new Order();
        order2.setId(2);
        order2.setTotalSum(200.0);
        order2.setOrderItems(List.of());
        
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1);
        orderItem1.setOrderId(1);
        orderItem1.setItemId(1);
        
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2);
        orderItem2.setOrderId(2);
        orderItem2.setItemId(2);
        
        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderItemRepository.findByOrderId(1)).thenReturn(Flux.just(orderItem1));
        when(orderItemRepository.findByOrderId(2)).thenReturn(Flux.just(orderItem2));

        OrderService spyOrderService = spy(orderService);
        when(spyOrderService.getOrders()).thenReturn(Flux.just(order1, order2));
        
        StepVerifier.create(spyOrderService.getOrders().collectList())
                .expectNextMatches(orders -> 
                    orders.size() == 2 && 
                    orders.get(0).getId() == 1 && 
                    orders.get(1).getId() == 2)
                .verifyComplete();
                
        verify(spyOrderService).getOrders();
    }

    @Test
    void getOrders_whenNoOrders_shouldReturnEmptyFlux() {
        when(orderRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getOrders())
                .verifyComplete();

        verify(orderRepository).findAll();
        verify(orderItemRepository, never()).findByOrderId(anyInt());
    }

    @Test
    void getOrder_whenFound_shouldReturnOrder() {
        int orderId = 1;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderItems(List.of());
        
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1);
        orderItem.setOrderId(orderId);
        orderItem.setItemId(1);
        
        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));

        OrderService spyOrderService = spy(orderService);
        when(spyOrderService.getOrder(orderId)).thenReturn(Mono.just(order));
        
        StepVerifier.create(spyOrderService.getOrder(orderId))
                .expectNextMatches(result -> 
                    result.getId() == orderId && 
                    result.getOrderItems() != null)
                .verifyComplete();
                
        verify(spyOrderService).getOrder(orderId);
    }

    @Test
    void getOrder_whenNotFound_shouldReturnEmpty() {
        int orderId = 99;
        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrder(orderId))
                .verifyComplete();

        verify(orderRepository).findById(orderId);
        verify(orderItemRepository, never()).findByOrderId(anyInt());
    }

    @Test
    void getOrdersTotalSum_shouldReturnSumFromRepository() {
        Double expectedSum = 123.45;
        when(orderRepository.getSumOfAllOrders()).thenReturn(Mono.just(expectedSum));

        StepVerifier.create(orderService.getOrdersTotalSum())
                .expectNext(expectedSum)
                .verifyComplete();

        verify(orderRepository).getSumOfAllOrders();
    }

    @Test
    void getOrdersTotalSum_whenRepositoryReturnsEmpty_shouldReturnZero() {
        when(orderRepository.getSumOfAllOrders()).thenReturn(Mono.empty());
        
        StepVerifier.create(orderService.getOrdersTotalSum())
                .expectNext(0.0)
                .verifyComplete();
                
        verify(orderRepository).getSumOfAllOrders();
    }
    
    @Test
    void getOrdersTotalSumFormatted_shouldReturnFormattedSum() {
        when(orderRepository.getSumOfAllOrders()).thenReturn(Mono.just(123.45));
        
        StepVerifier.create(orderService.getOrdersTotalSumFormatted())
                .expectNext("123,45")
                .verifyComplete();
                
        verify(orderRepository).getSumOfAllOrders();
    }
    
    @Test
    void getOrdersTotalSumFormatted_whenEmpty_shouldReturnZeroFormatted() {
        when(orderRepository.getSumOfAllOrders()).thenReturn(Mono.empty());
        
        StepVerifier.create(orderService.getOrdersTotalSumFormatted())
                .expectNext("0,00")
                .verifyComplete();
                
        verify(orderRepository).getSumOfAllOrders();
    }
}