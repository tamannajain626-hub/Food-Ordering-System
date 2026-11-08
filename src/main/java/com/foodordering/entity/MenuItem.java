package com.foodordering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * This class maps to the MySQL table called "menu_item".
 *
 * @Table(name = "menu_item")           -> the table name in MySQL
 * @Id                                  -> this field is the PRIMARY KEY
 * @GeneratedValue(strategy = IDENTITY) -> MySQL generates the id for us
 *                                        (it becomes an AUTO_INCREMENT column)
 */
@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    // BigDecimal is used for money, never double - so 12.50 stays exactly 12.50.
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // "available" tells us if the dish can be ordered right now.
    // columnDefinition makes MySQL use TINYINT(1) (a 0 / 1 column) instead of BIT.
    @Column(name = "available", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean available;

    public MenuItem() {
        // JPA needs a no-argument constructor to create the object for you.
    }

    public MenuItem(String name, String description, BigDecimal price,
                    String category, String imageUrl, boolean available) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "MenuItem{itemId=" + itemId
                + ", name='" + name + '\''
                + ", category='" + category + '\''
                + ", price=" + price
                + ", available=" + available + '}';
    }
}
