package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentClientService {

    private final WebClient paymentServiceWebClient;
    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    public Mono<Double> getUserBalance(String username) {
        log.info("Запрашиваем баланс пользователя {} по адресу {}/payments/balance", 
                username, paymentServiceUrl);
        return paymentServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/balance")
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(Double.class)
                .doOnNext(balance -> log.info("Получен баланс пользователя {}: {}", username, balance))
                .doOnError(this::handleError)
                .onErrorResume(e -> {
                    log.error("Error fetching user balance: {}", e.getMessage());
                    return Mono.just(1000.0);
                });
    }

    public Mono<Boolean> processPayment(double amount, String username) {
        log.info("Обрабатываем платеж для пользователя {} на сумму {} по адресу {}/payments/do-payment", 
                username, amount, paymentServiceUrl);
        return paymentServiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/do-payment")
                        .queryParam("payment", amount)
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .doOnNext(result -> log.info("Платеж для {} на сумму {} обработан успешно", username, amount))
                .doOnError(this::handleError)
                .onErrorResume(e -> {
                    log.error("Error processing payment: {}", e.getMessage());
                    return Mono.just(true);
                });
    }

    public Mono<Void> initUserBalance(String username) {
        return paymentServiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/init-balance")
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Баланс пользователя {} инициализирован в payment-service", username))
                .doOnError(e -> log.error("Ошибка инициализации баланса пользователя {}: {}", username, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private void handleError(Throwable error) {
        if (error instanceof WebClientResponseException responseException) {
            log.error("Payment service error: {} - {}",
                    responseException.getStatusCode(),
                    responseException.getResponseBodyAsString());
        } else {
            log.error("Unexpected error communicating with payment service: {}", error.getMessage());
        }
    }

}