package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PaymentClientService paymentClientService;
    @InjectMocks UserService userService;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testCreateUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(paymentClientService.initUserBalance(anyString())).thenReturn(Mono.empty());
        assertNotNull(userService.createUser(user).block());
    }

    @Test void testFindByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(user));
        assertNotNull(userService.findByUsername("user").block());
    }

} 