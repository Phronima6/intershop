package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Pages;
import ru.yandex.practicum.service.ItemService;
import org.springframework.web.server.ServerWebExchange;

@Controller
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemController {

    static int SEARCH_ITEMS_ON_PAGE = 100;
    ItemService itemService;

    @GetMapping({"/", "/main/items"})
    public Mono<Rendering> listItems(
            @RequestParam(name = "itemsOnPage", defaultValue = "${items.default.per.page:10}") int itemsOnPage,
            @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber) {
        return itemService.getPaginatedItems(itemsOnPage, pageNumber)
                .collectList()
                .zipWith(buildPagination(itemsOnPage))
                .map(tuple -> {
                    return Rendering.view("main")
                            .modelAttribute("items", tuple.getT1())
                            .modelAttribute("pages", tuple.getT2())
                            .build();
                });
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(@PathVariable int id) {
        return itemService.getItemById(id)
                .map(item -> Rendering.view("item")
                        .modelAttribute("itemDto", item)
                        .build())
                .switchIfEmpty(Mono.error(new RuntimeException("Item not found")));
    }

    @GetMapping("/search")
    public Mono<Rendering> searchItems(
            @RequestParam(name = "key", required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "NO") SortingCategory sort) {
        return itemService.searchItems(query, sort)
                .collectList()
                .zipWith(buildPagination(SEARCH_ITEMS_ON_PAGE))
                .map(tuple -> {
                    return Rendering.view("main")
                            .modelAttribute("items", tuple.getT1())
                            .modelAttribute("pages", tuple.getT2())
                            .build();
                });
    }

    @PostMapping("/item")
    public Mono<String> createItem(@Valid Item item, BindingResult result) {
        if (result.hasErrors()) {
            return Mono.just("items/new");
        }
        return itemService.createItem(item)
                .thenReturn("redirect:/main/items");
    }

    @PostMapping("/item/{id}/{action}")
    public Mono<String> updateQuantity(
            @PathVariable int id,
            @PathVariable String action,
            @RequestParam PageNames redirectTo,
            ServerWebExchange exchange) {
        int delta = "plus".equals(action) ? 1 : -1;
        return itemService.updateItemAmount(id, delta)
                .thenReturn(switch (redirectTo) {
                    case MAIN -> "redirect:/main/items";
                    case ITEM -> "redirect:/items/" + id;
                    case CART -> "redirect:/cart/items";
                })
                .onErrorResume(e -> {
                    exchange.getAttributes().put("errorMessage", e.getMessage());
                    return Mono.just(switch (redirectTo) {
                        case MAIN -> "redirect:/main/items";
                        case ITEM -> "redirect:/items/" + id;
                        case CART -> "redirect:/cart/items";
                    });
                });
    }

    private Mono<Pages> buildPagination(int itemsOnPage) {
        return itemService.getTotalItemsCount()
                .map(total -> {
                    int numberOfPages = (int) Math.ceil((double) total / itemsOnPage);
                    return Pages.builder()
                            .itemsOnPage(itemsOnPage)
                            .numberOfPages(numberOfPages)
                            .build();
                });
    }

}