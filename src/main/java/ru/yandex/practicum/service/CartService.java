package ru.yandex.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.util.Formatter;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartService {

    CartRepository cartRepository;
    ItemRepository itemRepository;

    public Mono<CartItem> addItemToCart(int itemId, int amount) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new RuntimeException("Item not found")))
                .flatMap(item -> {
                    if (item.getAmount() <= 0) {
                        return Mono.error(new IllegalStateException("Item out of stock"));
                    }
                    
                    return cartRepository.findByItemId(itemId)
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
                                    return cartRepository.save(newCartItem);
                                })
                            );
                });
    }

    public Mono<CartItem> addItemToCart(int itemId) {
        return addItemToCart(itemId, 1);
    }

    public Mono<Void> increaseCartItemQuantity(int cartItemId) {
        return cartRepository.findById(cartItemId)
                .switchIfEmpty(Mono.error(new RuntimeException("CartItem not found with id: " + cartItemId)))
                .flatMap(cartItem -> {
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    return cartRepository.save(cartItem);
                })
                .then();
    }

    public Mono<Void> decreaseCartItemQuantity(int cartItemId) {
        return cartRepository.findById(cartItemId)
                .switchIfEmpty(Mono.error(new RuntimeException("CartItem not found with id: " + cartItemId)))
                .flatMap(cartItem -> {
                    if (cartItem.getQuantity() > 1) {
                        cartItem.setQuantity(cartItem.getQuantity() - 1);
                        return cartRepository.save(cartItem);
                    } else {
                        return cartRepository.delete(cartItem).then(Mono.empty());
                    }
                })
                .then();
    }

    public Mono<Void> removeCartItemById(int cartItemId) {
        return cartRepository.existsById(cartItemId)
                .flatMap(exists -> exists ? 
                        cartRepository.deleteById(cartItemId) : 
                        Mono.empty())
                .then();
    }

    public Flux<CartItem> getCartItems() {
        return cartRepository.findAll()
                .flatMap(cartItem -> 
                    itemRepository.findById(cartItem.getItemId())
                            .map(item -> {
                                cartItem.setItem(item);
                                return cartItem;
                            })
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
}