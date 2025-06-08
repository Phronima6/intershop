package controller;

import config.WebClientConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.controller.PaymentController;
import ru.yandex.practicum.service.PaymentService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = PaymentController.class)
@WebFluxTest(PaymentController.class)
@Import(WebClientConfiguration.class)
public class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private PaymentService paymentService;


    @AfterEach
    void setUp() {
        Mockito.reset(paymentService);
    }

    @Test
    void getBalance_shouldReturnBalance() {
        double balance = 10_000;
        when(paymentService.getBalance("user"))
                .thenReturn(Mono.just(balance));
        webTestClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/payments/balance")
                                .queryParam("username", "user")
                                .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class).consumeWith(response -> {
                    Double body = response.getResponseBody();
                    assertNotNull(body);
                    assertEquals(body, balance, "Wrong balance value");
                });
        verify(paymentService, times(1)).getBalance("user");
    }

    @Test
    void doPayment_shouldDoPayment() {
        double balance = 10_000;
        when(paymentService.doPayment(balance, "user"))
                .thenReturn(Mono.empty());
        webTestClient.post()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/payments/do-payment")
                                .queryParam("payment", String.valueOf(balance))
                                .queryParam("username", "user")
                                .build())
                .exchange()
                .expectStatus().isOk();
        verify(paymentService, times(1)).doPayment(balance,"user");
    }

}