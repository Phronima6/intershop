package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.util.Formatter;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartService {

    CartRepository cartRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    Mono<Integer> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .flatMap(userRepository::findByUsername)
                .map(User::getId);
    }

    public Mono<CartItem> addItemToCart(int itemId, int amount) {
        return getCurrentUserId()
                .flatMap(userId -> 
                    itemRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Item not found")))
                        .flatMap(item -> {
                            if (item.getAmount() <= 0) {
                                return Mono.error(new IllegalStateException("Item out of stock"));
                            }
                            return cartRepository.findByItemIdAndUserId(itemId, userId)
                                    .take(1)
                                    .singleOrEmpty()
                                    .flatMap(cartItem -> {
                                        cartItem.setQuantity(cartItem.getQuantity() + amount);
                                        return cartRepository.save(cartItem);
                                    })
                                    .switchIfEmpty(
                                        Mono.defer(() -> {
                                            CartItem newCartItem = new CartItem();
                                            newCartItem.setItemId(item.getId());
                                            newCartItem.setQuantity(amount);
                                            newCartItem.setUserId(userId);
                                            return cartRepository.save(newCartItem);
                                        })
                                    );
                        })
                );
    }

    public Mono<CartItem> addItemToCart(int itemId) {
        return addItemToCart(itemId, 1);
    }

    public Mono<Void> increaseCartItemQuantity(int cartItemId) {
        return getCurrentUserId()
                .flatMap(userId ->
                    cartRepository.findByIdAndUserId(cartItemId, userId)
                        .switchIfEmpty(Mono.error(new RuntimeException("CartItem not found with id: " + cartItemId)))
                        .flatMap(cartItem -> {
                            cartItem.setQuantity(cartItem.getQuantity() + 1);
                            return cartRepository.save(cartItem);
                        })
                )
                .then();
    }

    public Mono<Void> decreaseCartItemQuantity(int cartItemId) {
        return getCurrentUserId()
                .flatMap(userId ->
                    cartRepository.findByIdAndUserId(cartItemId, userId)
                        .switchIfEmpty(Mono.error(new RuntimeException("CartItem not found with id: " + cartItemId)))
                        .flatMap(cartItem -> {
                            if (cartItem.getQuantity() > 1) {
                                cartItem.setQuantity(cartItem.getQuantity() - 1);
                                return cartRepository.save(cartItem);
                            } else {
                                return cartRepository.delete(cartItem).then(Mono.empty());
                            }
                        })
                )
                .then();
    }

    public Mono<Void> removeCartItemById(int cartItemId) {
        return getCurrentUserId()
                .flatMap(userId ->
                    cartRepository.existsByIdAndUserId(cartItemId, userId)
                        .flatMap(exists -> exists ? 
                                cartRepository.deleteById(cartItemId) : 
                                Mono.empty())
                )
                .then();
    }

    public Flux<CartItem> getCartItems() {
        return getCurrentUserId()
                .flatMapMany(userId ->
                    cartRepository.findByUserId(userId)
                        .flatMap(cartItem -> 
                            itemRepository.findById(cartItem.getItemId())
                                    .map(item -> {
                                        cartItem.setItem(item);
                                        return cartItem;
                                    })
                        )
                );
    }

    public Mono<Double> getTotalPrice() {
        return getCartItems()
                .map(cartItem -> cartItem.getItem().getPrice() * cartItem.getQuantity())
                .reduce(0.0, Double::sum);
    }

    public Mono<String> getFormattedTotalPrice() {
        return getTotalPrice()
                .map(Formatter.DECIMAL_FORMAT::format);
    }
    
    public Mono<Void> clearCart() {
        return getCurrentUserId()
                .flatMap(userId -> cartRepository.deleteByUserId(userId));
    }

}