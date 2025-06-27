package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.OrderService;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderController {

    OrderService orderService;
    CartService cartService;

    @PostMapping("/create-order")
    public Mono<Rendering> createOrder(ServerWebExchange exchange) {
        return orderService.createOrder()
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", order)
                        .build())
                .onErrorResume(e -> {
                    exchange.getAttributes().put("errorMessage", "Не удалось создать заказ: " + e.getMessage());
                    return Mono.just(Rendering.redirectTo("/cart/items").build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    exchange.getAttributes().put("errorMessage", "Корзина пуста или все товары отсутствуют на складе");
                    return Mono.just(Rendering.redirectTo("/cart/items").build());
                }));
    }

    @GetMapping("/orders")
    public Mono<Rendering> getOrders() {
        return orderService.getOrders()
                .collectList()
                .zipWith(orderService.getOrdersTotalSumFormatted())
                .flatMap(tuple -> 
                    orderService.getUserBalanceFormatted()
                        .map(balance -> Rendering.view("orders")
                            .modelAttribute("orders", tuple.getT1())
                            .modelAttribute("sumOfAllOrdersFormatted", tuple.getT2())
                            .modelAttribute("userBalance", balance)
                            .build())
                );
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrder(@PathVariable final Integer id) {
        return orderService.getOrder(id)
                .flatMap(order -> 
                    orderService.getUserBalanceFormatted()
                        .map(balance -> Rendering.view("order")
                            .modelAttribute("order", order)
                            .modelAttribute("userBalance", balance)
                            .build())
                )
                .switchIfEmpty(Mono.just(Rendering.redirectTo("/orders").build()));
    }
    
    @GetMapping("/user/balance")
    public Mono<Rendering> getUserBalance() {
        return orderService.getUserBalanceFormatted()
                .map(balance -> Rendering.view("balance")
                        .modelAttribute("userBalance", balance)
                        .build());
    }

}