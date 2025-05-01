package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.util.Formatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartService {

    CartRepository cartRepository;
    ItemRepository itemRepository;

    @Transactional
    public CartItem addItemToCart(int itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Товар не найден."));
        if (item.getAmount() <= 0) {
            throw new IllegalStateException("Товар отсутствует.");
        }
        return cartRepository.findByItemId(itemId)
                .stream()
                .findFirst()
                .map(cartItem -> {
                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                    return cartRepository.save(cartItem);
                })
                .orElseGet(() -> {
                    CartItem newCartItem = new CartItem();
                    newCartItem.setItem(item);
                    return cartRepository.save(newCartItem);
                });
    }

    @Transactional
    public void increaseCartItemQuantity(int cartItemId) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem не найден с id: " + cartItemId));

        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartRepository.save(cartItem);
    }

    @Transactional
    public void decreaseCartItemQuantity(int cartItemId) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem не найден с id: " + cartItemId));

        if (cartItem.getQuantity() > 1) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cartRepository.save(cartItem);
        } else {
            cartRepository.delete(cartItem);
        }
    }

    @Transactional
    public void removeCartItemById(int cartItemId) {
        if (!cartRepository.existsById(cartItemId)) {
            return;
        }
        cartRepository.deleteById(cartItemId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems() {
        return cartRepository.findAll();
    }

    @Transactional(readOnly = true)
    public double getTotalPrice() {
        return cartRepository.findAll()
                .stream()
                .mapToDouble(cartItem -> cartItem.getItem().getPrice() * cartItem.getQuantity())
                .sum();
    }

    public String getFormattedTotalPrice() {
        return Formatter.DECIMAL_FORMAT.format(getTotalPrice());
    }

}