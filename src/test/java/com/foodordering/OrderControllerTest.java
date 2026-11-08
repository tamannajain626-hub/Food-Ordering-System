package com.foodordering;

import com.foodordering.dto.OrderItemRequest;
import com.foodordering.dto.OrderRequest;
import com.foodordering.entity.Customer;
import com.foodordering.entity.MenuItem;
import com.foodordering.repository.CustomerRepository;
import com.foodordering.repository.MenuItemRepository;
import com.foodordering.repository.OrderItemRepository;
import com.foodordering.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private Customer customer;
    private MenuItem pizza;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        menuItemRepository.deleteAll();

        customer = customerRepository.save(new Customer("Test User", "test@example.com", "9876543210"));
        pizza = menuItemRepository.save(new MenuItem("Test Pizza", "Cheesy", new BigDecimal("12.50"), "Main Course", null, true));
    }

    @Test
    void placeOrder_shouldPersistOrderWithItemsAndCalculateTotal() throws Exception {
        String body = """
                {"customerId":%d,"items":[{"itemId":%d,"quantity":2}]}
                """.formatted(customer.getCustomerId(), pizza.getItemId());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.totalAmount").value(25.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderItems.length()").value(1))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderItems[0].priceAtOrdering").value(12.50))
                .andExpect(jsonPath("$.orderItems[0].lineTotal").value(25.00));

        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(orderItemRepository.findAll()).hasSize(1);
        assertThat(orderRepository.findAll().get(0).getTotalAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    void placeOrder_shouldRejectInvalidCustomerId() throws Exception {
        String body = """
                {"customerId":9999,"items":[{"itemId":%d,"quantity":1}]}
                """.formatted(pizza.getItemId());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_shouldRejectInvalidQuantity() throws Exception {
        String body = """
                {"customerId":%d,"items":[{"itemId":%d,"quantity":0}]}
                """.formatted(customer.getCustomerId(), pizza.getItemId());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_shouldRejectEmptyOrder() throws Exception {
        String body = """
                {"customerId":%d,"items":[]}
                """.formatted(customer.getCustomerId());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_shouldRejectUnavailableDish() throws Exception {
        MenuItem soldOut = menuItemRepository.save(new MenuItem("SoldOut", "x", new BigDecimal("5.00"), "Starters", null, false));
        String body = """
                {"customerId":%d,"items":[{"itemId":%d,"quantity":1}]}
                """.formatted(customer.getCustomerId(), soldOut.getItemId());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrders_shouldReturnOrdersWithItems() throws Exception {
        String body = """
                {"customerId":%d,"items":[{"itemId":%d,"quantity":1}]}
                """.formatted(customer.getCustomerId(), pizza.getItemId());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderItems.length()").value(1));
    }

    @Test
    void getOrders_shouldReturnEmptyArrayWhenNoOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
