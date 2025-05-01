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
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.service.ItemService;
import java.util.List;
import java.util.Optional;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ItemService itemService;
    Item item1;
    Item item2;

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public ItemService itemService() {
            return Mockito.mock(ItemService.class);
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
    void listItems_shouldReturnMainViewWithItemsAndPages() throws Exception {
        int pageNumber = 1;
        int itemsOnPage = 10;
        long totalItems = 25;
        given(itemService.getPaginatedItems(itemsOnPage, pageNumber))
                .willReturn(List.of(item1, item2));
        given(itemService.getTotalItemsCount()).willReturn(totalItems);
        mockMvc.perform(get("/main/items")
                        .param("itemsOnPage", String.valueOf(itemsOnPage))
                        .param("pageNumber", String.valueOf(pageNumber)))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("pages"))
                .andExpect(model().attribute("items", List.of(item1, item2)));
        verify(itemService).getPaginatedItems(itemsOnPage, pageNumber);
        verify(itemService).getTotalItemsCount();
    }

    @Test
    void getItem_shouldReturnItemViewWhenItemExists() throws Exception {
        int itemId = 1;
        given(itemService.getItemById(itemId)).willReturn(Optional.of(item1));
        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("itemDto"))
                .andExpect(model().attribute("itemDto", item1));
        verify(itemService).getItemById(itemId);
    }

    @Test
    void getItem_shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        int itemId = 99;
        given(itemService.getItemById(itemId)).willReturn(Optional.empty());
        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isNotFound());
        verify(itemService).getItemById(itemId);
    }

    @Test
    void searchItems_shouldReturnMainViewWithResults() throws Exception {
        String query = "Test";
        SortingCategory sort = SortingCategory.ALPHA;
        long totalItems = 2;
        given(itemService.searchItems(query, sort)).willReturn(List.of(item1, item2));
        given(itemService.getTotalItemsCount()).willReturn(totalItems);
        mockMvc.perform(get("/search")
                        .param("key", query)
                        .param("sort", sort.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("pages"))
                .andExpect(model().attribute("items", List.of(item1, item2)));
        verify(itemService).searchItems(query, sort);
        verify(itemService).getTotalItemsCount();
    }

    @Test
    void updateQuantity_shouldCallServiceAndRedirect() throws Exception {
        int itemId = 1;
        String action = "plus";
        int delta = 1;
        PageNames redirectTo = PageNames.CART;
        given(itemService.updateItemAmount(itemId, delta)).willReturn(item1);
        mockMvc.perform(post("/item/{id}/{action}", itemId, action)
                        .param("redirectTo", redirectTo.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
        verify(itemService).updateItemAmount(itemId, delta);
    }

}