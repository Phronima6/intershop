package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class WebClientConfiguration {

    @Bean
    WebClient webClient() {
        return WebClient.create();
    }

}