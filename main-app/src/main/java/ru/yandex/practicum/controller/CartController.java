package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.repository.ItemRepository;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartController {

    static String REDIRECT_MAIN = "redirect:/main/items";
    static String REDIRECT_CART = "redirect:/cart/items";
    CartService cartService;
    ItemRepository itemRepository;

    @PostMapping("/add/{id}")
    public Mono<String> addToCart(
            @PathVariable final Integer id,
            @RequestParam(required = false, defaultValue = "1") Integer amount,
            ServerWebExchange exchange) {
        System.out.println("Received amount parameter: " + amount);
        return itemRepository.findById(id)
                .flatMap(item -> {
                    int availableAmount = item.getAmount();
                    if (availableAmount <= 0) {
                        exchange.getAttributes().put("errorMessage", "Товар отсутствует на складе");
                        return Mono.just(REDIRECT_MAIN);
                    }
                    final int finalAmount = amount > availableAmount ? availableAmount : amount;
                    return cartService.addItemToCart(id, finalAmount)
                            .thenReturn(REDIRECT_MAIN)
                            .onErrorResume(e -> {
                                exchange.getAttributes().put("errorMessage", e.getMessage());
                                return Mono.just(REDIRECT_MAIN);
                            });
                })
                .defaultIfEmpty(REDIRECT_MAIN)
                .onErrorResume(e -> {
                    exchange.getAttributes().put("errorMessage", "Произошла ошибка: " + e.getMessage());
                    return Mono.just(REDIRECT_MAIN);
                });
    }

    @GetMapping("/items")
    public Mono<Rendering> viewCart() {
        return cartService.getCartItems()
                .collectList()
                .zipWith(cartService.getFormattedTotalPrice())
                .map(tuple -> Rendering.view("cart")
                        .modelAttribute("cartItems", tuple.getT1())
                        .modelAttribute("totalPriceFormatted", tuple.getT2())
                        .build());
    }

    @PostMapping("/item/{cartItemId}/plus")
    public Mono<String> increaseQuantity(@PathVariable int cartItemId, ServerWebExchange exchange) {
        return cartService.increaseCartItemQuantity(cartItemId)
                .thenReturn(REDIRECT_CART)
                .onErrorResume(e -> {
                    String errorMessage = e instanceof RuntimeException ? 
                                         "Элемент корзины не найден." : 
                                         "Не удалось увеличить количество: " + e.getMessage();
                    exchange.getAttributes().put("errorMessage", errorMessage);
                    return Mono.just(REDIRECT_CART);
                });
    }

    @PostMapping("/item/{cartItemId}/minus")
    public Mono<String> decreaseQuantity(@PathVariable int cartItemId, ServerWebExchange exchange) {
        return cartService.decreaseCartItemQuantity(cartItemId)
                .thenReturn(REDIRECT_CART)
                .onErrorResume(e -> {
                    String errorMessage = e instanceof RuntimeException ? 
                                         "Элемент корзины не найден." : 
                                         "Не удалось уменьшить количество: " + e.getMessage();
                    exchange.getAttributes().put("errorMessage", errorMessage);
                    return Mono.just(REDIRECT_CART);
                });
    }

    @PostMapping("/item/{cartItemId}/remove")
    public Mono<String> removeCartItemById(
            @PathVariable int cartItemId,
            @RequestParam PageNames redirectTo,
            @RequestParam(required = false) Integer itemId,
            ServerWebExchange exchange) {
        return cartService.removeCartItemById(cartItemId)
                .doOnSuccess(v -> exchange.getAttributes().put("successMessage", "Товар удален из корзины."))
                .onErrorResume(e -> {
                    exchange.getAttributes().put("errorMessage", "Не удалось удалить товар: " + e.getMessage());
                    return Mono.empty();
                })
                .thenReturn(switch (redirectTo) {
                    case MAIN -> REDIRECT_MAIN;
                    case ITEM -> itemId != null ? "redirect:/items/" + itemId : REDIRECT_MAIN;
                    case CART -> REDIRECT_CART;
                })
                .defaultIfEmpty(REDIRECT_CART);
    }

}