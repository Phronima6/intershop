package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.ItemService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemControllerTest {

    @Mock ItemService itemService;
    @Mock ServerWebExchange exchange;
    @InjectMocks ItemController itemController;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test void testListItems() {
        when(itemService.getPaginatedItems(anyInt(), anyInt())).thenReturn(Flux.empty());
        when(itemService.getTotalItemsCount()).thenReturn(Mono.just(0L));
        assertNotNull(itemController.listItems(10, 1).block());
    }

    @Test void testGetItem() {
        when(itemService.getItemById(anyInt())).thenReturn(Mono.empty());
        assertThrows(Exception.class, () -> itemController.getItem(1).block());
    }

} 