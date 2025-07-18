package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.util.Formatter;
import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
@RequiredArgsConstructor
public class OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    CartRepository cartRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;
    PaymentClientService paymentClientService;
    
    private Mono<Integer> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .flatMap(userRepository::findByUsername)
                .map(User::getId);
    }
    
    private Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }

    public Mono<Order> createOrder() {
        return getCurrentUserId()
                .flatMap(userId -> 
                    fetchCartItemsWithProducts(userId)
                        .collectList()
                        .flatMap(cartItems -> processCartItems(cartItems, userId))
                );
    }
    
    private Flux<CartItem> fetchCartItemsWithProducts(Integer userId) {
        return cartRepository.findByUserId(userId)
                .flatMap(cartItem -> 
                    itemRepository.findById(cartItem.getItemId())
                        .doOnError(e -> System.err.println("Error loading item: " + e.getMessage()))
                        .onErrorResume(e -> Mono.empty())
                        .map(item -> {
                            cartItem.setItem(item);
                            return cartItem;
                        })
                        .defaultIfEmpty(cartItem)
                );
    }
    
    private Mono<Order> processCartItems(List<CartItem> cartItems, Integer userId) {
        if (cartItems.isEmpty()) {
            return Mono.empty();
        }
        List<CartItem> validCartItems = filterValidCartItems(cartItems);
        if (validCartItems.isEmpty()) {
            return Mono.empty();
        }
        return createInitialOrder(validCartItems, userId);
    }
    
    private List<CartItem> filterValidCartItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .filter(cartItem -> cartItem.getItem() != null && cartItem.getItem().getAmount() > 0)
                .toList();
    }
    
    private Mono<Order> createInitialOrder(List<CartItem> validCartItems, Integer userId) {
        Order order = new Order();
        order.setUserId(userId);
        return orderRepository.save(order)
                .flatMap(savedOrder -> processOrderItems(savedOrder, validCartItems))
                .onErrorResume(e -> {
                    System.err.println("Error in order creation: " + e.getMessage());
                    return Mono.empty();
                });
    }
    
    private Mono<Order> processOrderItems(Order savedOrder, List<CartItem> validCartItems) {
        Flux<OrderItem> orderItemsFlux = createOrderItemsFromCart(savedOrder, validCartItems);
        return orderItemsFlux
                .collectList()
                .flatMap(orderItems -> finalizeOrder(savedOrder, orderItems, validCartItems));
    }
    
    private Flux<OrderItem> createOrderItemsFromCart(Order savedOrder, List<CartItem> validCartItems) {
        return Flux.fromIterable(validCartItems)
                .flatMap(cartItem -> {
                    if (cartItem.getItem() == null) {
                        return Mono.empty();
                    }
                    if (cartItem.getQuantity() > cartItem.getItem().getAmount()) {
                        return Mono.error(new IllegalStateException(
                                "Недостаточно товара '" + cartItem.getItem().getName() + 
                                "' на складе. Доступно: " + cartItem.getItem().getAmount() + 
                                ", запрошено: " + cartItem.getQuantity()));
                    }
                    return createAndSaveOrderItem(savedOrder, cartItem);
                });
    }
    
    private Mono<OrderItem> createAndSaveOrderItem(Order savedOrder, CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(savedOrder.getId());
        orderItem.setItemId(cartItem.getItemId());
        orderItem.setItemAmount(cartItem.getQuantity());
        cartItem.getItem().setAmount(cartItem.getItem().getAmount() - cartItem.getQuantity());
        return itemRepository.save(cartItem.getItem())
                .flatMap(savedItem -> orderItemRepository.save(orderItem))
                .onErrorResume(e -> {
                    System.err.println("Error saving order item: " + e.getMessage());
                    return Mono.empty();
                });
    }
    
    private Mono<Order> finalizeOrder(Order savedOrder, List<OrderItem> orderItems, List<CartItem> validCartItems) {
        if (orderItems.isEmpty()) {
            return orderRepository.deleteById(savedOrder.getId())
                    .then(Mono.empty());
        }
        savedOrder.setOrderItems(orderItems);
        double totalSum = calculateTotalSum(orderItems, validCartItems);
        savedOrder.setTotalSum(totalSum);
        savedOrder.setPaid(false);
        return orderRepository.save(savedOrder)
                .onErrorResume(e -> {
                    System.err.println("Error saving order: " + e.getMessage());
                    return Mono.empty();
                })
                .flatMap(this::clearUserCartAndReturnOrder);
    }
    
    private double calculateTotalSum(List<OrderItem> orderItems, List<CartItem> validCartItems) {
        return orderItems.stream()
                .mapToDouble(orderItem -> {
                    CartItem cartItem = validCartItems.stream()
                            .filter(ci -> ci.getItemId().equals(orderItem.getItemId()))
                            .findFirst()
                            .orElse(null);
                    if (cartItem == null || cartItem.getItem() == null) {
                        return 0;
                    }
                    return orderItem.getItemAmount() * cartItem.getItem().getPrice();
                })
                .sum();
    }
    
    private Mono<Order> clearUserCartAndReturnOrder(Order savedOrderWithTotal) {
        return getCurrentUserId()
                .flatMap(userId -> 
                    cartRepository.deleteByUserId(userId)
                        .thenReturn(savedOrderWithTotal)
                        .onErrorResume(e -> {
                            System.err.println("Error clearing cart: " + e.getMessage());
                            return Mono.just(savedOrderWithTotal);
                        })
                );
    }

    public Flux<Order> getOrders() {
        return getCurrentUserId()
                .flatMapMany(userId -> 
                    orderRepository.findByUserId(userId)
                        .flatMap(order -> 
                            orderItemRepository.findByOrderId(order.getId())
                                .flatMap(orderItem -> 
                                    itemRepository.findById(orderItem.getItemId())
                                        .doOnError(e -> System.err.println("Error loading item: " + e.getMessage()))
                                        .onErrorResume(e -> Mono.empty())
                                        .map(item -> {
                                            orderItem.setItem(item);
                                            return orderItem;
                                        })
                                        .defaultIfEmpty(orderItem)
                                )
                                .collectList()
                                .map(orderItems -> {
                                    order.setOrderItems(orderItems);
                                    return order;
                                })
                        )
                );
    }

    public Mono<Order> getOrder(int id) {
        return getCurrentUserId()
                .flatMap(userId -> 
                    orderRepository.findByIdAndUserId(id, userId)
                        .flatMap(order -> 
                            orderItemRepository.findByOrderId(order.getId())
                                .flatMap(orderItem -> 
                                    itemRepository.findById(orderItem.getItemId())
                                        .doOnError(e -> System.err.println("Error loading item: " + e.getMessage()))
                                        .onErrorResume(e -> Mono.empty())
                                        .map(item -> {
                                            orderItem.setItem(item);
                                            return orderItem;
                                        })
                                        .defaultIfEmpty(orderItem)
                                )
                                .collectList()
                                .map(orderItems -> {
                                    order.setOrderItems(orderItems);
                                    return order;
                                })
                        )
                );
    }
    
    public Mono<Double> getUserBalance() {
        return getCurrentUsername()
                .flatMap(paymentClientService::getUserBalance);
    }
    
    public Mono<String> getUserBalanceFormatted() {
        return getUserBalance().map(balance -> Formatter.DECIMAL_FORMAT.format(balance));
    }

    private Mono<Boolean> doesCartHaveNotNullItems(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getItem().getAmount() != 0) {
                return Mono.just(true);
            }
        }
        return Mono.just(false);
    }

    public Mono<Double> getOrdersTotalSum() {
        return getCurrentUserId()
                .flatMap(orderRepository::getSumOfAllOrdersByUserId)
                .defaultIfEmpty(0.0);
    }

    public Mono<String> getOrdersTotalSumFormatted() {
        return getOrdersTotalSum().map(Formatter.DECIMAL_FORMAT::format);
    }

}