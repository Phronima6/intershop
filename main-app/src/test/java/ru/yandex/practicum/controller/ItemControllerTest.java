package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.model.Pages;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.service.ItemCacheService;
import ru.yandex.practicum.service.ItemService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private ItemService itemService;

    private Item item1;
    private Item item2;

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public ItemService itemService() {
            return Mockito.mock(ItemService.class);
        }
        
        @Bean
        public ItemCacheService itemCacheService() {
            return Mockito.mock(ItemCacheService.class);
        }
        
        @Bean
        public ItemRepository itemRepository() {
            return Mockito.mock(ItemRepository.class);
        }
        
        @Bean
        public ImageRepository imageRepository() {
            return Mockito.mock(ImageRepository.class);
        }
        
        @Bean
        public OrderRepository orderRepository() {
            return Mockito.mock(OrderRepository.class);
        }
        
        @Bean
        public OrderItemRepository orderItemRepository() {
            return Mockito.mock(OrderItemRepository.class);
        }
        
        @Bean
        public CartRepository cartRepository() {
            return Mockito.mock(CartRepository.class);
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(itemService);
        item1 = new Item();
        item1.setId(1);
        item1.setName("Test Item 1");
        item1.setDescription("Description 1");
        item1.setPrice(10);
        item1.setAmount(5);

        item2 = new Item();
        item2.setId(2);
        item2.setName("Test Item 2");
        item2.setDescription("Description 2");
        item2.setPrice(20);
        item2.setAmount(10);
    }

    @Test
    void listItems_shouldReturnMainViewWithItemsAndPages() {
        int pageNumber = 1;
        int itemsOnPage = 10;
        long totalItems = 25;
        
        Pages pages = Pages.builder()
                .itemsOnPage(itemsOnPage)
                .numberOfPages(3)
                .build();

        given(itemService.getPaginatedItems(itemsOnPage, pageNumber))
                .willReturn(Flux.just(item1, item2));
        given(itemService.getTotalItemsCount())
                .willReturn(Mono.just(totalItems));

        webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/main/items")
                        .queryParam("itemsOnPage", itemsOnPage)
                        .queryParam("pageNumber", pageNumber)
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getPaginatedItems(itemsOnPage, pageNumber);
        verify(itemService).getTotalItemsCount();
    }

    @Test
    void getItem_shouldReturnItemViewWhenItemExists() {
        int itemId = 1;
        given(itemService.getItemById(itemId)).willReturn(Mono.just(item1));

        webClient.get().uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getItemById(itemId);
    }

    @Test
    void getItem_shouldReturnNotFoundWhenItemDoesNotExist() {
        int itemId = 99;
        given(itemService.getItemById(itemId))
                .willReturn(Mono.error(new RuntimeException("Item not found")));

        webClient.get().uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(itemService).getItemById(itemId);
    }

    @Test
    void searchItems_shouldReturnMainViewWithResults() {
        String query = "Test";
        SortingCategory sort = SortingCategory.ALPHA;
        long totalItems = 2;

        given(itemService.searchItems(query, sort))
                .willReturn(Flux.just(item1, item2));
        given(itemService.getTotalItemsCount())
                .willReturn(Mono.just(totalItems));

        webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("key", query)
                        .queryParam("sort", sort.name())
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemService).searchItems(query, sort);
        verify(itemService).getTotalItemsCount();
    }

    @Test
    void updateQuantity_shouldCallServiceAndRedirect() {
        int itemId = 1;
        String action = "plus";
        int delta = 1;
        PageNames redirectTo = PageNames.CART;

        given(itemService.updateItemAmount(itemId, delta))
                .willReturn(Mono.just(item1));

        webClient.post().uri(uriBuilder -> uriBuilder
                        .path("/item/{id}/{action}")
                        .queryParam("redirectTo", redirectTo.name())
                        .build(itemId, action))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(itemService).updateItemAmount(itemId, delta);
    }
}