package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    private Item item1;
    private Item itemOutOfStock;
    private CartItem cartItemExisting;
    private CartItem cartItemNew;

    @BeforeEach
    void setUp() {
        item1 = new Item();
        item1.setId(1);
        item1.setName("Test Item 1");
        item1.setPrice(10);
        item1.setAmount(5);

        itemOutOfStock = new Item();
        itemOutOfStock.setId(2);
        itemOutOfStock.setName("Out of Stock Item");
        itemOutOfStock.setPrice(5);
        itemOutOfStock.setAmount(0);

        cartItemExisting = new CartItem();
        cartItemExisting.setId(101);
        cartItemExisting.setItemId(item1.getId());
        cartItemExisting.setQuantity(2);
        cartItemExisting.setItem(item1);

        cartItemNew = new CartItem();
        cartItemNew.setItemId(item1.getId());
        cartItemNew.setQuantity(1);
        cartItemNew.setItem(item1);
    }

    @Test
    void addItemToCart_whenItemExistsAndInStock_andNotInCart_shouldAddNewCartItem() {
        when(itemRepository.findById(item1.getId())).thenReturn(Mono.just(item1));
        when(cartRepository.findByItemId(item1.getId())).thenReturn(Flux.empty());
        when(cartRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            saved.setId(102);
            return Mono.just(saved);
        });

        StepVerifier.create(cartService.addItemToCart(item1.getId()))
                .expectNextMatches(cartItem -> {
                    return cartItem.getItemId().equals(item1.getId()) &&
                           cartItem.getQuantity() == 1 &&
                           cartItem.getId() == 102;
                })
                .verifyComplete();

        verify(itemRepository).findById(item1.getId());
        verify(cartRepository).findByItemId(item1.getId());
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_whenItemExistsAndInStock_andAlreadyInCart_shouldIncrementQuantity() {
        when(itemRepository.findById(item1.getId())).thenReturn(Mono.just(item1));
        when(cartRepository.findByItemId(item1.getId())).thenReturn(Flux.just(cartItemExisting));
        
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(cartItemExisting.getId());
        updatedCartItem.setItemId(cartItemExisting.getItemId());
        updatedCartItem.setQuantity(3);
        updatedCartItem.setItem(item1);
        
        when(cartRepository.save(any(CartItem.class))).thenReturn(Mono.just(updatedCartItem));

        StepVerifier.create(cartService.addItemToCart(item1.getId()))
                .expectNextMatches(cartItem -> 
                    cartItem.getQuantity() == 3 && 
                    cartItem.getId().equals(cartItemExisting.getId()))
                .verifyComplete();

        verify(itemRepository).findById(item1.getId());
        verify(cartRepository).findByItemId(item1.getId());
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_whenItemNotFound_shouldReturnError() {
        when(itemRepository.findById(99)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.addItemToCart(99))
                .expectError(RuntimeException.class)
                .verify();

        verify(itemRepository).findById(99);
        verify(cartRepository, never()).findByItemId(anyInt());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemToCart_whenItemOutOfStock_shouldReturnError() {
        when(itemRepository.findById(itemOutOfStock.getId())).thenReturn(Mono.just(itemOutOfStock));

        StepVerifier.create(cartService.addItemToCart(itemOutOfStock.getId()))
                .expectError(IllegalStateException.class)
                .verify();

        verify(itemRepository).findById(itemOutOfStock.getId());
        verify(cartRepository, never()).findByItemId(anyInt());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void increaseCartItemQuantity_whenItemExists_shouldIncrementAndSave() {
        when(cartRepository.findById(cartItemExisting.getId())).thenReturn(Mono.just(cartItemExisting));
        
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(cartItemExisting.getId());
        updatedCartItem.setItemId(cartItemExisting.getItemId());
        updatedCartItem.setQuantity(3);
        
        when(cartRepository.save(any(CartItem.class))).thenReturn(Mono.just(updatedCartItem));

        StepVerifier.create(cartService.increaseCartItemQuantity(cartItemExisting.getId()))
                .verifyComplete();

        verify(cartRepository).findById(cartItemExisting.getId());
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void increaseCartItemQuantity_whenItemNotFound_shouldReturnError() {
        when(cartRepository.findById(999)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.increaseCartItemQuantity(999))
                .expectError(RuntimeException.class)
                .verify();

        verify(cartRepository).findById(999);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void decreaseCartItemQuantity_whenItemExistsAndQuantityMoreThanOne_shouldDecrementAndSave() {
        when(cartRepository.findById(cartItemExisting.getId())).thenReturn(Mono.just(cartItemExisting));
        
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setId(cartItemExisting.getId());
        updatedCartItem.setItemId(cartItemExisting.getItemId());
        updatedCartItem.setQuantity(1);
        
        when(cartRepository.save(any(CartItem.class))).thenReturn(Mono.just(updatedCartItem));

        StepVerifier.create(cartService.decreaseCartItemQuantity(cartItemExisting.getId()))
                .verifyComplete();

        verify(cartRepository).findById(cartItemExisting.getId());
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void decreaseCartItemQuantity_whenItemExistsAndQuantityIsOne_shouldDelete() {
        CartItem cartItemWithOne = new CartItem();
        cartItemWithOne.setId(103);
        cartItemWithOne.setItemId(item1.getId());
        cartItemWithOne.setQuantity(1);
        
        when(cartRepository.findById(cartItemWithOne.getId())).thenReturn(Mono.just(cartItemWithOne));
        when(cartRepository.delete(cartItemWithOne)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.decreaseCartItemQuantity(cartItemWithOne.getId()))
                .verifyComplete();

        verify(cartRepository).findById(cartItemWithOne.getId());
        verify(cartRepository, never()).save(any());
        verify(cartRepository).delete(cartItemWithOne);
    }

    @Test
    void decreaseCartItemQuantity_whenItemNotFound_shouldReturnError() {
        when(cartRepository.findById(999)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.decreaseCartItemQuantity(999))
                .expectError(RuntimeException.class)
                .verify();

        verify(cartRepository).findById(999);
        verify(cartRepository, never()).save(any());
        verify(cartRepository, never()).delete(any());
    }

    @Test
    void removeCartItemById_whenItemExists_shouldDelete() {
        int cartItemId = 101;
        when(cartRepository.existsById(cartItemId)).thenReturn(Mono.just(true));
        when(cartRepository.deleteById(cartItemId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeCartItemById(cartItemId))
                .verifyComplete();

        verify(cartRepository).existsById(cartItemId);
        verify(cartRepository).deleteById(cartItemId);
    }

    @Test
    void removeCartItemById_whenItemNotExists_shouldDoNothing() {
        int cartItemId = 999;
        when(cartRepository.existsById(cartItemId)).thenReturn(Mono.just(false));

        StepVerifier.create(cartService.removeCartItemById(cartItemId))
                .verifyComplete();

        verify(cartRepository).existsById(cartItemId);
        verify(cartRepository, never()).deleteById(anyInt());
    }

    @Test
    void getCartItems_shouldReturnAllItems() {
        CartItem cartItem1 = new CartItem();
        cartItem1.setId(101);
        cartItem1.setItemId(1);
        cartItem1.setQuantity(2);

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(102);
        cartItem2.setItemId(2);
        cartItem2.setQuantity(1);

        when(cartRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(1)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2)).thenReturn(Mono.just(itemOutOfStock));

        StepVerifier.create(cartService.getCartItems())
                .expectNextMatches(cartItem -> 
                    cartItem.getId() == 101 && 
                    cartItem.getItem().getId() == 1 && 
                    cartItem.getQuantity() == 2)
                .expectNextMatches(cartItem -> 
                    cartItem.getId() == 102 &&
                    cartItem.getItem().getId() == 2 &&
                    cartItem.getQuantity() == 1)
                .verifyComplete();

        verify(cartRepository).findAll();
        verify(itemRepository).findById(1);
        verify(itemRepository).findById(2);
    }

    @Test
    void getTotalPrice_shouldCalculateCorrectly() {
        CartItem cartItem1 = new CartItem();
        cartItem1.setId(101);
        cartItem1.setItemId(1);
        cartItem1.setQuantity(2);
        cartItem1.setItem(item1);

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(102);
        cartItem2.setItemId(2);
        cartItem2.setQuantity(1);
        
        Item item2 = new Item();
        item2.setId(2);
        item2.setPrice(20);
        cartItem2.setItem(item2);

        when(cartRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(1)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2)).thenReturn(Mono.just(item2));

        StepVerifier.create(cartService.getTotalPrice())
                .expectNext(2 * 10.0 + 1 * 20.0) // 40.0
                .verifyComplete();
    }

    @Test
    void getFormattedTotalPrice_shouldFormatCorrectly() {
        CartItem cartItem = new CartItem();
        cartItem.setId(101);
        cartItem.setItemId(1);
        cartItem.setQuantity(2);
        cartItem.setItem(item1);

        when(cartRepository.findAll()).thenReturn(Flux.just(cartItem));
        when(itemRepository.findById(1)).thenReturn(Mono.just(item1));

        StepVerifier.create(cartService.getFormattedTotalPrice())
                .expectNext("20,00")
                .verifyComplete();
    }
}