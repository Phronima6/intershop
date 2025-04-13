package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.model.PageNames;
import ru.yandex.practicum.model.SortingCategory;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Pages;
import ru.yandex.practicum.service.ItemService;
import java.io.IOException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemController {

    static int SEARCH_ITEMS_ON_PAGE = 100;
    ItemService itemService;

    @GetMapping({"/", "/main/items"})
    public String listItems(
            Model model,
            @RequestParam(name = "itemsOnPage", defaultValue = "${items.default.per.page:10}") int itemsOnPage,
            @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber) {
        model.addAttribute("items", itemService.getPaginatedItems(itemsOnPage, pageNumber));
        model.addAttribute("pages", buildPagination(itemsOnPage));
        return "main";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable int id, Model model) {
        Item item = itemService.getItemById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found"));
        model.addAttribute("itemDto", item);
        return "item";
    }

    @GetMapping("/search")
    public String searchItems(
            Model model,
            @RequestParam(name = "key", required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "NO") SortingCategory sort) {
        model.addAttribute("items", itemService.searchItems(query, sort));
        model.addAttribute("pages", buildPagination(SEARCH_ITEMS_ON_PAGE));
        return "main";
    }

    @PostMapping("/item")
    public String createItem(@Valid Item item, BindingResult result) throws IOException {
        if (result.hasErrors()) {
            return "items/new";
        }
        itemService.createItem(item);
        return "redirect:/main/items";
    }

    @PostMapping("/item/{id}/{action}")
    public String updateQuantity(
            @PathVariable int id,
            @PathVariable String action,
            @RequestParam PageNames redirectTo) {
        int delta = "plus".equals(action) ? 1 : -1;
        itemService.updateItemAmount(id, delta);
        return switch (redirectTo) {
            case MAIN -> "redirect:/main/items";
            case ITEM -> "redirect:/items/" + id;
            case CART -> "redirect:/cart/items";
        };
    }

    private Pages buildPagination(int itemsOnPage) {
        return Pages.builder()
                .itemsOnPage(itemsOnPage)
                .numberOfPages(calculatePages(itemsOnPage))
                .build();
    }

    private int calculatePages(int itemsOnPage) {
        long total = itemService.getTotalItemsCount();
        return (int) Math.ceil((double) total / itemsOnPage);
    }

}