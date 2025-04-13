package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.service.OrderService;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderController {

    OrderService orderService;

    @PostMapping("/create-order")
    public String createOrder(final Model model) {
        final Order order = orderService.createOrder();
        if (order == null) {
            return "redirect:/main/items";
        }
        model.addAttribute("order", order);
        return "order";
    }

    @GetMapping("/orders")
    public String getOrders(final Model model) {
        final List<Order> orders = orderService.getOrders();
        final String sumOfAllOrdersFormatted = orderService.getOrdersTotalSumFormatted();
        model.addAttribute("sumOfAllOrdersFormatted", sumOfAllOrdersFormatted);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(final Model model, @PathVariable final Integer id) {
        final Order order = orderService.getOrder(id);
        model.addAttribute("order", order);
        return "order";
    }

}