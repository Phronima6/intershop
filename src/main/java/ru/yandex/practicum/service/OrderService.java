package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.util.Formatter;
import java.util.ArrayList;
import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
@RequiredArgsConstructor
public class OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    CartRepository cartRepository;

    public Order createOrder() {
        List<CartItem> cartItems = cartRepository.findAll();
        if (cartItems.isEmpty() || !doesCartHaveNotNullItems(cartItems)) {
            return null;
        }
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = new Order();
        Order savedOrder = orderRepository.save(order);
        double totalSum = 0;
        for (CartItem cartItem : cartItems) {
            Item item = cartItem.getItem();
            OrderItem orderItem;
            if (item.getAmount() != 0) {
                orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setItem(item);
                orderItem.setItemAmount(item.getAmount());
                orderItems.add(orderItem);
                orderItemRepository.save(orderItem);
                totalSum += orderItem.getItemAmount() * orderItem.getItem().getPrice();
            }
        }
        savedOrder.setOrderItems(orderItems);
        savedOrder.setTotalSum(totalSum);
        orderRepository.save(savedOrder);
        cartRepository.deleteAll();
        return savedOrder;
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(int id) {
        return orderRepository.findById(id).get();
    }

    private boolean doesCartHaveNotNullItems(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getItem().getAmount() != 0) {
                return true;
            }
        }
        return false;
    }

    public Double getOrdersTotalSum() {
        return orderRepository.getSumOfAllOrders();
    }

    public String getOrdersTotalSumFormatted() {
        Double sumOfAllOrders = getOrdersTotalSum();
        return Formatter.DECIMAL_FORMAT.format(sumOfAllOrders != null ? sumOfAllOrders : 0);
    }

}