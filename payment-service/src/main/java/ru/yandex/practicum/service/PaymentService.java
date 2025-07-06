package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final int BALANCE_MULTIPLIER = 10_000_000;
    private static Map<String, Double> usersBalances = new HashMap<>();

    @PostConstruct
    public void init() {
        usersBalances.put("user", 1_000_000.0);
        log.info("Initialized user balance: {}", usersBalances.get("user"));
    }

    public Mono<Double> getBalance(String username) {
        if (usersBalances.containsKey(username)) {
            log.debug("Getting balance for user {}: {}", username, usersBalances.get(username));
            return Mono.just(usersBalances.get(username));
        } else {
            Double balance = Math.random() * BALANCE_MULTIPLIER;
            balance = (double) Math.round(balance * 100) / 100;
            usersBalances.put(username, balance);
            log.info("Created new balance for user {}: {}", username, balance);
            return Mono.just(balance);
        }
    }

    public Mono<Void> doPayment(double payment, String username) {
        double balance = usersBalances.get(username);
        balance -= payment;
        usersBalances.put(username, balance);
        log.debug("Payment processed for user {}: -{}, new balance: {}", username, payment, balance);
        return Mono.empty();
    }

    public Mono<Void> initBalance(String username) {
        if (!usersBalances.containsKey(username)) {
            double balance = 1000.0;
            usersBalances.put(username, balance);
            log.info("Инициализирован баланс для пользователя {}: {}", username, balance);
        }
        return Mono.empty();
    }

}