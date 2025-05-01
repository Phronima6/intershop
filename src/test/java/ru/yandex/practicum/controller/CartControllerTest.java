package ru.yandex.practicum.controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.service.CartService;
import java.util.List;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CartService cartService;
    CartItem cartItem;
    Item item;

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public CartService cartService() {
            return Mockito.mock(CartService.class);
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(cartService);
        item = new Item();
        item.setId(1);
        item.setName("Test Item 1");
        item.setPrice(10);
        cartItem = new CartItem();
        cartItem.setId(101);
        cartItem.setItem(item);
        cartItem.setQuantity(2);
    }

    @Test
    void addToCart_shouldCallServiceAndRedirect() throws Exception {
        int itemId = 1;
        given(cartService.addItemToCart(itemId)).willReturn(cartItem);
        mockMvc.perform(post("/cart/add/{id}", itemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(cartService).addItemToCart(itemId);
    }

    @Test
    void addToCart_shouldHandleExceptionAndRedirect() throws Exception {
        int itemId = 1;
        String errorMessage = "Item out of stock";
        given(cartService.addItemToCart(itemId)).willThrow(new IllegalStateException(errorMessage));
        mockMvc.perform(post("/cart/add/{id}", itemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"))
                .andExpect(flash().attribute("errorMessage", errorMessage));
        verify(cartService).addItemToCart(itemId);
    }

    @Test
    void viewCart_shouldReturnCartViewWithItems() throws Exception {
        given(cartService.getCartItems()).willReturn(List.of(cartItem));
        given(cartService.getFormattedTotalPrice()).willReturn("20,00");
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cartItems"))
                .andExpect(model().attributeExists("totalPriceFormatted"))
                .andExpect(model().attribute("cartItems", List.of(cartItem)));
        verify(cartService).getCartItems();
        verify(cartService).getFormattedTotalPrice();
    }

    @Test
    void increaseQuantity_shouldCallServiceAndRedirect() throws Exception {
        int cartItemId = 101;
        doNothing().when(cartService).increaseCartItemQuantity(cartItemId);
        mockMvc.perform(post("/cart/item/{cartItemId}/plus", cartItemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
        verify(cartService).increaseCartItemQuantity(cartItemId);
    }

    @Test
    void decreaseQuantity_shouldCallServiceAndRedirect() throws Exception {
        int cartItemId = 101;
        doNothing().when(cartService).decreaseCartItemQuantity(cartItemId);
        mockMvc.perform(post("/cart/item/{cartItemId}/minus", cartItemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
        verify(cartService).decreaseCartItemQuantity(cartItemId);
    }

    @Test
    void removeCartItemById_shouldCallServiceAndRedirect() throws Exception {
        int cartItemId = 101;
        PageNames redirectTo = PageNames.CART;
        doNothing().when(cartService).removeCartItemById(cartItemId);
        mockMvc.perform(post("/cart/item/{cartItemId}/remove", cartItemId)
                        .param("redirectTo", redirectTo.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"))
                .andExpect(flash().attributeExists("successMessage"));
        verify(cartService).removeCartItemById(cartItemId);
    }

}