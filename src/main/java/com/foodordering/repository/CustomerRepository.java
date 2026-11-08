package com.foodordering.repository;

import com.foodordering.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the "customer" table.
 *
 * JpaRepository already gives us save(), findAll(), findById(), delete()...
 * We only add the two small queries we need ourselves.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Finds a customer by email (ignoring upper/lower case). */
    Optional<Customer> findByEmailIgnoreCase(String email);

    /** Returns true when a customer with that email already exists. */
    boolean existsByEmailIgnoreCase(String email);
}
