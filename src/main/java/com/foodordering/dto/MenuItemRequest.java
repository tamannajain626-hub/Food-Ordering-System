package com.foodordering.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * The JSON body the client sends when adding a new dish to the menu.
 *
 * We use a separate class (a "DTO") instead of the MenuItem entity so that
 * the client can never set the item_id by hand - the id must come from MySQL.
 */
public class MenuItemRequest {

    @NotBlank(message = "Food name is required")
    @Size(max = 100, message = "Food name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price can have at most 2 decimal places")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must be at most 50 characters")
    private String category;

    @Size(max = 500, message = "Image URL must be at most 500 characters")
    private String imageUrl;

    // Optional. When the client does not send it, the dish is available.
    private Boolean available;

    public MenuItemRequest() {
    }

    public MenuItemRequest(String name, String description, BigDecimal price,
                           String category, String imageUrl, Boolean available) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
