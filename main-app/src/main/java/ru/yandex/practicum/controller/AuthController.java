package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final ReactiveAuthenticationManager authenticationManager;

    @GetMapping("/login")
    public Mono<Rendering> loginPage(@RequestParam(required = false) String error) {
        return Mono.just(Rendering.view("login")
                .modelAttribute("error", error != null)
                .build());
    }

    @GetMapping("/register")
    public Mono<Rendering> registerPage() {
        return Mono.just(Rendering.view("register").build());
    }

    @PostMapping("/register")
    public Mono<Rendering> registerUser(ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String username = formData.getFirst("username");
                    String password = formData.getFirst("password");
                    String email = formData.getFirst("email");
                    log.info("Регистрация нового пользователя: {}, email: {}", username, email);
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.setEnabled(true);
                    user.setRoles("ROLE_USER");
                    return userService.createUser(user)
                            .flatMap(savedUser -> {
                                log.info("Пользователь успешно создан: {}", savedUser.getUsername());
                                UsernamePasswordAuthenticationToken authToken =
                                        new UsernamePasswordAuthenticationToken(username, password);
                                return authenticationManager.authenticate(authToken)
                                        .flatMap(authentication -> {
                                            SecurityContextImpl securityContext = new SecurityContextImpl();
                                            securityContext.setAuthentication(authentication);
                                            return exchange.getSession()
                                                    .doOnNext(session -> session.getAttributes().put("SPRING_SECURITY_CONTEXT", securityContext))
                                                    .thenReturn(Rendering.redirectTo("/main/items").build());
                                        });
                            })
                            .onErrorResume(e -> {
                                log.error("Ошибка при создании пользователя: {}", e.getMessage());
                                return Mono.just(
                                        Rendering.view("register")
                                                .modelAttribute("error", e.getMessage())
                                                .build()
                                );
                            });
                });
    }

    @GetMapping("/logout")
    public Mono<Rendering> logout(WebSession session) {
        return session.invalidate().thenReturn(Rendering.redirectTo("/login").build());
    }

} 