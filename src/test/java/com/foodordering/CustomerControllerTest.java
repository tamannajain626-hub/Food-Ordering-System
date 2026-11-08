package com.foodordering;

import com.foodordering.entity.Customer;
import com.foodordering.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the customer REST API.
 *
 * These tests use an in-memory H2 database (see src/test/resources/application.properties)
 * so that they can be run any time, with or without a MySQL server.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void clearDatabase() {
        customerRepository.deleteAll();
    }

    private static String json(String name, String email, String phone) {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "phone": "%s"
                }
                """.formatted(name, email, phone);
    }

    @Test
    void registerCustomer_shouldSaveRowAndReturnGeneratedId() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tamanna", "tamanna@example.com", "9876543210")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").isNumber())
                .andExpect(jsonPath("$.name").value("Tamanna"))
                .andExpect(jsonPath("$.email").value("tamanna@example.com"))
                .andExpect(jsonPath("$.phone").value("9876543210"));

        // The row really is in the database, with a generated id.
        List<Customer> saved = customerRepository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getCustomerId()).isNotNull().isGreaterThan(0L);
        assertThat(saved.get(0).getName()).isEqualTo("Tamanna");
    }

    @Test
    void registerCustomer_shouldGiveEachCustomerADifferentId() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("First", "first@example.com", "9876543210")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Second", "second@example.com", "9876543211")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").isNumber());

        List<Customer> saved = customerRepository.findAll();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getCustomerId()).isNotEqualTo(saved.get(1).getCustomerId());
    }

    @Test
    void getAllCustomers_shouldReturnSavedCustomers() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tamanna", "tamanna@example.com", "9876543210")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Tamanna"))
                .andExpect(jsonPath("$[0].email").value("tamanna@example.com"))
                .andExpect(jsonPath("$[0].customerId").isNumber());
    }

    @Test
    void registerCustomer_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tamanna", "tamanna@example.com", "9876543210")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Someone Else", "tamanna@example.com", "9999999999")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A customer with email 'tamanna@example.com' already exists"));

        // The duplicate was NOT stored, still only one row.
        assertThat(customerRepository.findAll()).hasSize(1);
    }

    @Test
    void registerCustomer_shouldReturn400_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tamanna", "not-an-email", "9876543210")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").value("Email must be a valid email address"));

        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Test
    void registerCustomer_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "tamanna@example.com", "9876543210")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name is required"));
    }

    @Test
    void registerCustomer_shouldReturn400_whenPhoneIsInvalid() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Tamanna", "tamanna@example.com", "12345")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phone").value("Phone must be exactly 10 digits"));
    }

    @Test
    void getAllCustomers_shouldReturnEmptyList_whenNoCustomers() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
