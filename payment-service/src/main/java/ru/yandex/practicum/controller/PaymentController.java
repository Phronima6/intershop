package ru.yandex.practicum.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/balance")
    public Mono<Double> getBalance(@RequestParam String username) {
        return paymentService.getBalance(username);
    }

    @PostMapping("/do-payment")
    public Mono<Void> doPayment(
            @Parameter(description = "Сумма заказа для списания с баланса") @RequestParam double payment,
            @RequestParam String username) {
        return paymentService.doPayment(payment, username);
    }

    @PostMapping("/init-balance")
    public Mono<Void> initBalance(@RequestParam String username) {
        return paymentService.initBalance(username);
    }

}