package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class CartServiceTest {

    @Mock
    CartRepository cartRepository;
    @Mock
    ItemRepository itemRepository;
    @InjectMocks
    CartService cartService;
    Item item;
    Item itemOutOfStock;
    CartItem cartItemExisting;
    CartItem cartItemNew;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1);
        item.setName("Test Item 1");
        item.setPrice(10);
        item.setAmount(5);
        itemOutOfStock = new Item();
        itemOutOfStock.setId(2);
        itemOutOfStock.setName("Out of Stock Item");
        itemOutOfStock.setPrice(5);
        itemOutOfStock.setAmount(0);
        cartItemExisting = new CartItem();
        cartItemExisting.setId(101);
        cartItemExisting.setItem(item);
        cartItemExisting.setQuantity(2);
        cartItemNew = new CartItem();
        cartItemNew.setItem(item);
        cartItemNew.setQuantity(1);
    }

    @Test
    void addItemToCart_whenItemExistsAndInStock_andNotInCart_shouldAddNewCartItem() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(cartRepository.findByItemId(item.getId())).thenReturn(List.of());
        when(cartRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            saved.setId(102);
            return saved;
        });
        CartItem result = cartService.addItemToCart(item.getId());
        assertNotNull(result);
        assertEquals(item, result.getItem());
        assertEquals(1, result.getQuantity());
        verify(itemRepository).findById(item.getId());
        verify(cartRepository).findByItemId(item.getId());
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_whenItemExistsAndInStock_andAlreadyInCart_shouldIncrementQuantity() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(cartRepository.findByItemId(item.getId())).thenReturn(List.of(cartItemExisting));
        when(cartRepository.save(cartItemExisting)).thenReturn(cartItemExisting);
        CartItem result = cartService.addItemToCart(item.getId());
        assertNotNull(result);
        assertEquals(3, result.getQuantity());
        assertEquals(cartItemExisting.getId(), result.getId());
        verify(itemRepository).findById(item.getId());
        verify(cartRepository).findByItemId(item.getId());
        verify(cartRepository).save(cartItemExisting);
    }

    @Test
    void addItemToCart_whenItemNotFound_shouldThrowException() {
        when(itemRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            cartService.addItemToCart(99);
        });
        verify(itemRepository).findById(99);
        verify(cartRepository, never()).findByItemId(anyInt());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemToCart_whenItemOutOfStock_shouldThrowException() {
        when(itemRepository.findById(itemOutOfStock.getId())).thenReturn(Optional.of(itemOutOfStock));
        assertThrows(IllegalStateException.class, () -> {
            cartService.addItemToCart(itemOutOfStock.getId());
        });
        verify(itemRepository).findById(itemOutOfStock.getId());
        verify(cartRepository, never()).findByItemId(anyInt());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void increaseCartItemQuantity_whenItemExists_shouldIncrementAndSave() {
        when(cartRepository.findById(cartItemExisting.getId())).thenReturn(Optional.of(cartItemExisting));
        when(cartRepository.save(cartItemExisting)).thenReturn(cartItemExisting);
        cartService.increaseCartItemQuantity(cartItemExisting.getId());
        assertEquals(3, cartItemExisting.getQuantity());
        verify(cartRepository).findById(cartItemExisting.getId());
        verify(cartRepository).save(cartItemExisting);
    }

    @Test
    void increaseCartItemQuantity_whenItemNotFound_shouldThrowException() {
        when(cartRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            cartService.increaseCartItemQuantity(999);
        });
        verify(cartRepository).findById(999);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void decreaseCartItemQuantity_whenItemExistsAndQuantityMoreThanOne_shouldDecrementAndSave() {
        when(cartRepository.findById(cartItemExisting.getId())).thenReturn(Optional.of(cartItemExisting));
        when(cartRepository.save(cartItemExisting)).thenReturn(cartItemExisting);
        cartService.decreaseCartItemQuantity(cartItemExisting.getId());
        assertEquals(1, cartItemExisting.getQuantity());
        verify(cartRepository).findById(cartItemExisting.getId());
        verify(cartRepository).save(cartItemExisting);
    }

    @Test
    void decreaseCartItemQuantity_whenItemExistsAndQuantityIsOne_shouldDelete() {
        CartItem cartItemWithOne = new CartItem(103, item, 1);
        when(cartRepository.findById(cartItemWithOne.getId())).thenReturn(Optional.of(cartItemWithOne));
        doNothing().when(cartRepository).delete(cartItemWithOne);
        cartService.decreaseCartItemQuantity(cartItemWithOne.getId());
        verify(cartRepository).findById(cartItemWithOne.getId());
        verify(cartRepository, never()).save(any());
        verify(cartRepository).delete(cartItemWithOne);
    }

    @Test
    void decreaseCartItemQuantity_whenItemNotFound_shouldThrowException() {
        when(cartRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            cartService.decreaseCartItemQuantity(999);
        });
        verify(cartRepository).findById(999);
        verify(cartRepository, never()).save(any());
        verify(cartRepository, never()).delete(any());
    }

    @Test
    void removeCartItemById_whenItemExists_shouldDelete() {
        int cartItemId = 101;
        when(cartRepository.existsById(cartItemId)).thenReturn(true);
        doNothing().when(cartRepository).deleteById(cartItemId);
        cartService.removeCartItemById(cartItemId);
        verify(cartRepository).existsById(cartItemId);
        verify(cartRepository).deleteById(cartItemId);
    }

    @Test
    void removeCartItemById_whenItemNotExists_shouldDoNothing() {
        int cartItemId = 999;
        when(cartRepository.existsById(cartItemId)).thenReturn(false);
        cartService.removeCartItemById(cartItemId);
        verify(cartRepository).existsById(cartItemId);
        verify(cartRepository, never()).deleteById(anyInt());
    }

    @Test
    void getCartItems_shouldReturnAllItems() {
        List<CartItem> expectedItems = List.of(cartItemExisting);
        when(cartRepository.findAll()).thenReturn(expectedItems);
        List<CartItem> actualItems = cartService.getCartItems();
        assertEquals(expectedItems, actualItems);
        verify(cartRepository).findAll();
    }

}