package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.OrderService;
import ru.yandex.practicum.service.PaymentService;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class PaymentController {

    OrderService orderService;
    PaymentService paymentService;

    @GetMapping("/orders/{orderId}/payment")
    public Mono<Rendering> showPaymentPage(@PathVariable Integer orderId, ServerWebExchange exchange) {
        return orderService.getOrder(orderId)
                .flatMap(order -> 
                    orderService.getUserBalanceFormatted()
                        .map(balance -> Rendering.view("payment")
                            .modelAttribute("order", order)
                            .modelAttribute("orderId", order.getId())
                            .modelAttribute("amount", order.getTotalSumFormatted())
                            .modelAttribute("userBalance", balance)
                            .modelAttribute("errorMessage", 
                                exchange.getAttributeOrDefault("errorMessage", null))
                            .build())
                )
                .switchIfEmpty(Mono.just(Rendering.redirectTo("/orders").build()));
    }

    @PostMapping("/process-payment")
    public Mono<Rendering> processPayment(ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    try {
                        String orderIdStr = formData.getFirst("orderId");
                        if (orderIdStr == null || orderIdStr.isEmpty()) {
                            log.error("ID заказа не указан в форме оплаты");
                            return Mono.just(Rendering.redirectTo("/orders").build());
                        }
                        Integer orderId = Integer.valueOf(orderIdStr);
                        log.info("Получен запрос на оплату заказа ID: {}", orderId);
                        return exchange.getPrincipal()
                            .map(principal -> principal.getName())
                            .flatMap(username -> paymentService.processPaymentForOrder(orderId, username))
                            .flatMap(success -> {
                                if (success) {
                                    log.info("Заказ {} успешно оплачен", orderId);
                                    return Mono.just(Rendering.redirectTo("/orders/" + orderId)
                                            .build());
                                } else {
                                    log.warn("Ошибка при оплате заказа {}", orderId);
                                    exchange.getAttributes().put("errorMessage", 
                                            "Не удалось выполнить оплату. Недостаточно средств или ошибка платежного сервиса.");
                                    return Mono.just(Rendering.redirectTo("/orders/" + orderId + "/payment")
                                            .build());
                                }
                            });
                    } catch (Exception e) {
                        log.error("Ошибка при обработке формы оплаты: {}", e.getMessage());
                        return Mono.just(Rendering.redirectTo("/orders").build());
                    }
                });
    }

}