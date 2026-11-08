package com.foodordering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Food Ordering System.
 *
 * Run it with:  mvn spring-boot:run
 * Or build it:  mvn clean package  ->  java -jar target/food-ordering-1.0.0.jar
 */
@SpringBootApplication
public class FoodOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodOrderingApplication.class, args);
    }
}
