package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class PaymentService {

    PaymentClientService paymentClientService;
    OrderRepository orderRepository;
    private static final String DEFAULT_USERNAME = "user";
    

    @Transactional
    public Mono<Boolean> processPaymentForOrder(Integer orderId) {
        log.info("Начинаем обработку платежа для заказа ID: {}", orderId);
        return orderRepository.findById(orderId)
            .doOnNext(order -> log.info("Заказ {} найден, сумма: {}, статус оплаты: {}", 
                    orderId, order.getTotalSum(), order.isPaid()))
            .flatMap(order -> {
                if (order.isPaid()) {
                    log.info("Заказ {} уже оплачен", orderId);
                    return Mono.just(true);
                }
                return paymentClientService.getUserBalance(DEFAULT_USERNAME)
                    .doOnNext(balance -> log.info("Текущий баланс пользователя {}: {}", DEFAULT_USERNAME, balance))
                    .flatMap(balance -> {
                        double orderAmount = order.getTotalSum();
                        if (balance >= orderAmount) {
                            log.info("Достаточно средств для оплаты: баланс {}, сумма заказа {}", 
                                    balance, orderAmount);
                            return processPayment(order, orderAmount);
                        } else {
                            log.warn("Недостаточно средств для оплаты заказа {}: баланс {}, требуется {}", 
                                orderId, balance, orderAmount);
                            return Mono.just(false);
                        }
                    });
            })
            .doOnError(error -> log.error("Ошибка при обработке платежа: {}", error.getMessage()))
            .onErrorResume(e -> {
                log.error("Обработано исключение при выполнении платежа: {}", e.getMessage(), e);
                return Mono.just(false);
            })
            .switchIfEmpty(Mono.fromCallable(() -> {
                log.error("Заказ с ID {} не найден", orderId);
                return false;
            }));
    }

    private Mono<Boolean> processPayment(Order order, double amount) {
        log.info("Вызов платежного сервиса для заказа {}, сумма {}", order.getId(), amount);
        return paymentClientService.processPayment(amount, DEFAULT_USERNAME)
            .doOnNext(success -> {
                if (success) {
                    log.info("Платежный сервис вернул успешный результат для заказа {}", order.getId());
                } else {
                    log.error("Платежный сервис вернул ошибку для заказа {}", order.getId());
                }
            })
            .flatMap(success -> {
                if (success) {
                    log.info("Обновляем статус оплаты для заказа {}", order.getId());
                    order.setPaid(true);
                    return orderRepository.save(order)
                            .doOnNext(savedOrder -> log.info("Статус заказа {} успешно обновлен", savedOrder.getId()))
                            .doOnError(e -> log.error("Ошибка при сохранении заказа {}: {}", 
                                    order.getId(), e.getMessage()))
                            .map(savedOrder -> true)
                            .onErrorResume(e -> {
                                log.error("Обработано исключение при сохранении статуса заказа {}: {}", 
                                        order.getId(), e.getMessage(), e);
                                return Mono.just(false);
                            });
                } else {
                    log.error("Ошибка при оплате заказа {}: сумма {}", order.getId(), amount);
                    return Mono.just(false);
                }
            });
    }

}