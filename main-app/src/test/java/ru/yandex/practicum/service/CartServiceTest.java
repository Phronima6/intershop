package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock ItemRepository itemRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CartService cartService;
    CartService cartServiceSpy;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        cartServiceSpy = spy(new CartService(cartRepository, itemRepository, userRepository));
        doReturn(Mono.just(1)).when(cartServiceSpy).getCurrentUserId();
    }

    @Test void testGetCartItems() {
        when(cartRepository.findAll()).thenReturn(Flux.empty());
        assertNotNull(cartService.getCartItems().collectList().block());
    }

    @Test void testAddToCart() {
        ru.yandex.practicum.model.Item item = new ru.yandex.practicum.model.Item();
        item.setId(1);
        item.setAmount(10);
        when(itemRepository.findById(anyInt())).thenReturn(Mono.just(item));
        when(cartRepository.findByItemIdAndUserId(anyInt(), anyInt())).thenReturn(Flux.empty());
        when(cartRepository.save(any())).thenReturn(Mono.just(new CartItem()));
        assertNotNull(cartServiceSpy.addItemToCart(1, 1).block());
    }

} 