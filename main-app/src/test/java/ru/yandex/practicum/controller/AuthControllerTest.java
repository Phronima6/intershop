package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.UserService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock UserService userService;
    @Mock ReactiveAuthenticationManager authenticationManager;
    @Mock ServerWebExchange exchange;
    @Mock WebSession session;
    @InjectMocks AuthController authController;

    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }

    @Test void testRegisterPage() {
        assertNotNull(authController.registerPage().block());
    }

    @Test void testLoginPage() {
        assertNotNull(authController.loginPage(null).block());
    }

    @Test void testLogout() {
        when(session.invalidate()).thenReturn(Mono.empty());
        assertNotNull(authController.logout(session).block());
    }

} 