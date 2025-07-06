package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {

    @InjectMocks MainController mainController;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testCurrentUser() {
        Mono<Authentication> result = mainController.currentUser();
        assertNotNull(result);
    }

    @Test void testIsAuthenticated() {
        Mono<Boolean> result = mainController.isAuthenticated();
        assertNotNull(result);
    }

} 