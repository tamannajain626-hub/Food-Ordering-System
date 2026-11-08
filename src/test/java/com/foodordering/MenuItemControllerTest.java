package com.foodordering;

import com.foodordering.entity.MenuItem;
import com.foodordering.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the menu REST API.
 *
 * These tests use an in-memory H2 database (see src/test/resources/application.properties)
 * so that they can be run any time, with or without a MySQL server.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @BeforeEach
    void clearDatabase() {
        menuItemRepository.deleteAll();
    }

    private static String json(String name, String price, String category, boolean available) {
        return """
                {
                  "name": "%s",
                  "description": "A very tasty dish",
                  "price": %s,
                  "category": "%s",
                  "imageUrl": "https://example.com/food.jpg",
                  "available": %s
                }
                """.formatted(name, price, category, available);
    }

    @Test
    void addMenuItem_shouldSaveRowAndReturnGeneratedId() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Margherita Pizza", "12.50", "Main Course", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").isNumber())
                .andExpect(jsonPath("$.name").value("Margherita Pizza"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.category").value("Main Course"))
                .andExpect(jsonPath("$.available").value(true));

        // The row really is in the database, with a generated id.
        List<MenuItem> saved = menuItemRepository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getItemId()).isNotNull().isGreaterThan(0L);
        assertThat(saved.get(0).getPrice()).isEqualByComparingTo("12.50");
    }

    @Test
    void getAllMenuItems_shouldReturnSavedItems() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cappuccino", "3.50", "Drinks", true)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/menu-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cappuccino"))
                .andExpect(jsonPath("$[0].itemId").isNumber());
    }

    @Test
    void getAllMenuItems_shouldReturnEmptyList_whenThereIsNoMenu() throws Exception {
        mockMvc.perform(get("/api/menu-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMenuItemsByCategory_shouldOnlyReturnThatCategory() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cappuccino", "3.50", "Drinks", true)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tiramisu", "6.75", "Desserts", true)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/menu-items/category/Drinks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cappuccino"));
    }

    @Test
    void getCategories_shouldReturnEachCategoryOnlyOnce() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cappuccino", "3.50", "Drinks", true)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Latte", "4.00", "Drinks", true)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tiramisu", "6.75", "Desserts", true)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/menu-items/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Desserts"))
                .andExpect(jsonPath("$[1]").value("Drinks"));
    }

    @Test
    void getMenuItem_shouldReturn404_whenTheIdIsUnknown() throws Exception {
        mockMvc.perform(get("/api/menu-items/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No menu item found with id 9999"));
    }

    @Test
    void getMenuItem_shouldReturn400_whenTheIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/menu-items/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMenuItem_shouldReturn409_whenTheNameAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cappuccino", "3.50", "Drinks", true)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cappuccino", "4.00", "Drinks", true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A menu item called 'Cappuccino' already exists"));

        // The duplicate was NOT stored, still only one row.
        assertThat(menuItemRepository.findAll()).hasSize(1);
    }

    @Test
    void addMenuItem_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "12.50", "Main Course", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("Food name is required"));

        assertThat(menuItemRepository.findAll()).isEmpty();
    }

    @Test
    void addMenuItem_shouldReturn400_whenPriceIsMissing() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cappuccino",
                                  "category": "Drinks"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("Price is required"));
    }

    @Test
    void addMenuItem_shouldReturn400_whenPriceIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cappuccino", "0", "Drinks", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("Price must be greater than 0"));
    }

    @Test
    void addMenuItem_shouldReturn400_whenCategoryIsBlank() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cappuccino", "3.50", "", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.category").value("Category is required"));
    }

    @Test
    void setAvailability_shouldMarkTheDishAsSoldOut() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Soup", "5.50", "Starters", true)))
                .andExpect(status().isCreated());

        Long itemId = menuItemRepository.findAll().get(0).getItemId();

        mockMvc.perform(put("/api/menu-items/" + itemId + "/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "available": false }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.available").value(false));

        assertThat(menuItemRepository.findById(itemId).orElseThrow().isAvailable()).isFalse();
    }

    @Test
    void updateMenuItem_shouldChangeTheDish() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Old Name", "5.00", "Starters", true)))
                .andExpect(status().isCreated());

        Long itemId = menuItemRepository.findAll().get(0).getItemId();

        mockMvc.perform(put("/api/menu-items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("New Name", "9.99", "Desserts", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.category").value("Desserts"));

        assertThat(menuItemRepository.findAll()).hasSize(1);
        assertThat(menuItemRepository.findById(itemId).orElseThrow().getName()).isEqualTo("New Name");
    }

    @Test
    void deleteMenuItem_shouldRemoveTheRow() throws Exception {
        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Soup", "5.50", "Starters", true)))
                .andExpect(status().isCreated());

        Long itemId = menuItemRepository.findAll().get(0).getItemId();

        mockMvc.perform(delete("/api/menu-items/" + itemId))
                .andExpect(status().isNoContent());

        assertThat(menuItemRepository.findAll()).isEmpty();
    }
}
