package com.foodordering.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * This class maps to the MySQL table called "order_item".
 *
 * @Table(name = "order_item")          -> the table name in MySQL
 * @Id                                  -> this field is the PRIMARY KEY
 * @GeneratedValue(strategy = IDENTITY) -> MySQL generates the id for us
 *                                        (it becomes an AUTO_INCREMENT column)
 */
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MenuItem menuItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Price saved at the moment of ordering - never trust browser totals
    @Column(name = "price_at_ordering", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtOrdering;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    public OrderItem() {
        // JPA needs a no-argument constructor to create the object for you.
    }

    public OrderItem(MenuItem menuItem, Integer quantity, BigDecimal priceAtOrdering) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.priceAtOrdering = priceAtOrdering;
        this.lineTotal = priceAtOrdering.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtOrdering() {
        return priceAtOrdering;
    }

    public void setPriceAtOrdering(BigDecimal priceAtOrdering) {
        this.priceAtOrdering = priceAtOrdering;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    @Override
    public String toString() {
        return "OrderItem{orderItemId=" + orderItemId
                + ", menuItemId=" + (menuItem != null ? menuItem.getItemId() : null)
                + ", quantity=" + quantity
                + ", priceAtOrdering=" + priceAtOrdering
                + ", lineTotal=" + lineTotal + '}';
    }
}
