package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentClientService {

    private final WebClient paymentServiceWebClient;

    public Mono<Double> getUserBalance(String username) {
        return paymentServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/balance")
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(Double.class)
                .doOnError(this::handleError)
                .onErrorResume(e -> {
                    log.error("Error fetching user balance: {}", e.getMessage());
                    return Mono.just(0.0);
                });
    }

    public Mono<Boolean> processPayment(double amount, String username) {
        return paymentServiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/do-payment")
                        .queryParam("payment", amount)
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .doOnError(this::handleError)
                .onErrorResume(e -> {
                    log.error("Error processing payment: {}", e.getMessage());
                    return Mono.just(false);
                });
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