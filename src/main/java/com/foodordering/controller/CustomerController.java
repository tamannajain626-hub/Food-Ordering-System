package com.foodordering.controller;

import com.foodordering.dto.CustomerRequest;
import com.foodordering.entity.Customer;
import com.foodordering.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for customers.
 *
 *  POST /api/customers  -> register a new customer (returns the generated customer_id)
 *  GET  /api/customers  -> list all customers
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Registers a new customer.
     *
     * @Valid makes Spring check the fields of the request body and return
     *         a 400 Bad Request if something is wrong (empty name, bad email...).
     */
    @PostMapping
    public ResponseEntity<Customer> registerCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer savedCustomer = customerService.registerCustomer(request);
        // 201 Created + the saved customer (including the generated customer_id).
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    /**
     * Returns every customer that is stored in the database.
     */
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}
