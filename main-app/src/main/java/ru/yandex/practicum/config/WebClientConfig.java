package ru.yandex.practicum.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Value("${payment.service.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${payment.service.read-timeout:5000}")
    private int readTimeout;

    @Value("${payment.service.write-timeout:5000}")
    private int writeTimeout;

    @Bean
    public WebClient paymentServiceWebClient() {
        log.info("Configuring WebClient for payment service at: {}", paymentServiceUrl);
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));
        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}