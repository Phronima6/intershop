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
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.OrderService;
import java.util.List;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrderService orderService;
    @Autowired
    CartService cartService;
    Order order;

    @TestConfiguration
    static class ControllerTestConfiguration {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }
        @Bean
        public CartService cartService() {
            return Mockito.mock(CartService.class);
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(orderService, cartService);
        order = new Order();
        order.setId(1);
    }

    @Test
    void createOrder_shouldReturnOrderViewWhenOrderCreated() throws Exception {
        given(orderService.createOrder()).willReturn(order);
        mockMvc.perform(post("/create-order"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", order));
        verify(orderService).createOrder();
    }

    @Test
    void createOrder_shouldRedirectWhenOrderIsNull() throws Exception {
        given(orderService.createOrder()).willReturn(null);
        mockMvc.perform(post("/create-order"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
        verify(orderService).createOrder();
    }

    @Test
    void getOrders_shouldReturnOrdersView() throws Exception {
        String formattedSum = "100,50";
        given(orderService.getOrders()).willReturn(List.of(order));
        given(orderService.getOrdersTotalSumFormatted()).willReturn(formattedSum);
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("sumOfAllOrdersFormatted"))
                .andExpect(model().attribute("orders", List.of(order)))
                .andExpect(model().attribute("sumOfAllOrdersFormatted", formattedSum));
        verify(orderService).getOrders();
        verify(orderService).getOrdersTotalSumFormatted();
    }

    @Test
    void getOrder_shouldReturnOrderViewWhenFound() throws Exception {
        int orderId = 1;
        given(orderService.getOrder(orderId)).willReturn(order);
        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", order));
        verify(orderService).getOrder(orderId);
    }

}