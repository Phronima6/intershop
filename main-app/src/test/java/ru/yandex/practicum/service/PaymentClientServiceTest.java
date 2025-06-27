package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentClientServiceTest {

    @Mock WebClient webClient;
    @Mock RequestBodyUriSpec requestBodyUriSpec;
    @Mock RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock ResponseSpec responseSpec;
    @InjectMocks PaymentClientService paymentClientService;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestBodyUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
        when(responseSpec.bodyToMono(Double.class)).thenReturn(Mono.just(1000.0));
    }

    @Test void testInitUserBalance() {
        assertNotNull(paymentClientService.initUserBalance("testuser"));
    }

} 