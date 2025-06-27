package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentClientService paymentClientService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .disabled(!user.isEnabled())
                        .authorities(user.getRoleSet().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(java.util.stream.Collectors.toList())
                        )
                        .build()
                );
    }

    public Mono<User> createUser(User user) {
        log.info("Создание пользователя: {}", user.getUsername());
        return userRepository.existsByUsername(user.getUsername())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.error("Пользователь с именем {} уже существует", user.getUsername());
                        return Mono.error(new RuntimeException("Пользователь с таким именем уже существует"));
                    }
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    if (user.getRoles() == null || user.getRoles().isEmpty()) {
                        user.setRoles("ROLE_USER");
                    }
                    log.info("Сохраняем пользователя: {}", user.getUsername());
                    return userRepository.save(user)
                        .flatMap(savedUser -> paymentClientService.initUserBalance(savedUser.getUsername())
                            .thenReturn(savedUser));
                });
    }

    public Mono<User> getCurrentUserEntity(String username) {
        return userRepository.findByUsername(username);
    }

} 