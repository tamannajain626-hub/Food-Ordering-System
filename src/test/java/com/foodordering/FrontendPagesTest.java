package com.foodordering;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that the frontend files are served by Spring Boot.
 *
 * The pages live in src/main/resources/static/ and are served automatically:
 *   http://localhost:8080/              -> index.html (the registration form)
 *   http://localhost:8080/customers.html -> the customer list page
 *
 * Note: MockMvc does not follow the internal "forward" that Spring Boot uses
 * for the welcome page, so we test the real file paths directly.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FrontendPagesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrationPage_shouldServeTheForm() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("registerForm")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"email\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"phone\"")));
    }

    @Test
    void customersPage_shouldServeTheListPage() throws Exception {
        mockMvc.perform(get("/customers.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("customersTableBody")));
    }

    @Test
    void stylesheetAndScripts_shouldBeServed() throws Exception {
        mockMvc.perform(get("/css/style.css")).andExpect(status().isOk());
        mockMvc.perform(get("/js/register.js")).andExpect(status().isOk());
        mockMvc.perform(get("/js/customers.js")).andExpect(status().isOk());
    }

    @Test
    void menuPage_shouldServeTheMenu() throws Exception {
        mockMvc.perform(get("/menu.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("menuGrid")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("categoryFilters")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("cartItems")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("subtotal")));
    }

    @Test
    void menuStylesheetAndScript_shouldBeServed() throws Exception {
        mockMvc.perform(get("/css/menu.css")).andExpect(status().isOk());
        mockMvc.perform(get("/js/menu.js")).andExpect(status().isOk());
    }

    @Test
    void everyPage_shouldLinkToTheMenu() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"menu.html\"")));
        mockMvc.perform(get("/customers.html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"menu.html\"")));
    }

    @Test
    void homePage_shouldRespond() throws Exception {
        // In a browser this shows the registration form.
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }
}
