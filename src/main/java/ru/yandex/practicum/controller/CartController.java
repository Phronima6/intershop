package ru.yandex.practicum.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartController {

    static String REDIRECT_MAIN = "redirect:/main/items";
    static String REDIRECT_CART = "redirect:/cart/items";
    CartService cartService;

    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable final Integer id, RedirectAttributes redirectAttributes) {
        try {
            cartService.addItemToCart(id);
            redirectAttributes.addFlashAttribute("successMessage", "Товар добавлен в корзину!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось добавить товар: " + e.getMessage());
        }
        return REDIRECT_MAIN;
    }

    @GetMapping("/items")
    public String viewCart(final Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPriceFormatted", cartService.getFormattedTotalPrice());
        return "cart";
    }

    @PostMapping("/item/{cartItemId}/plus")
    public String increaseQuantity(@PathVariable int cartItemId, RedirectAttributes redirectAttributes) {
        try {
            cartService.increaseCartItemQuantity(cartItemId);
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Элемент корзины не найден.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось увеличить количество: " + e.getMessage());
        }
        return REDIRECT_CART;
    }

    @PostMapping("/item/{cartItemId}/minus")
    public String decreaseQuantity(@PathVariable int cartItemId, RedirectAttributes redirectAttributes) {
        try {
            cartService.decreaseCartItemQuantity(cartItemId);
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Элемент корзины не найден.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось уменьшить количество: " + e.getMessage());
        }
        return REDIRECT_CART;
    }

    @PostMapping("/item/{cartItemId}/remove")
    public String removeCartItemById(
            @PathVariable int cartItemId,
            @RequestParam PageNames redirectTo,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeCartItemById(cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Товар удален из корзины.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось удалить товар: " + e.getMessage());
        }
        return switch (redirectTo) {
            case MAIN -> REDIRECT_MAIN;
            case ITEM -> "redirect:/items/" + "?";
            case CART -> REDIRECT_CART;
        };
    }

}