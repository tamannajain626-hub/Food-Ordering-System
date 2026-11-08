package com.foodordering.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long orderItemId;
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal priceAtOrdering;
    private BigDecimal lineTotal;

    public OrderItemResponse() {
    }

    public OrderItemResponse(Long orderItemId, Long itemId, String itemName, Integer quantity,
                             BigDecimal priceAtOrdering, BigDecimal lineTotal) {
        this.orderItemId = orderItemId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.priceAtOrdering = priceAtOrdering;
        this.lineTotal = lineTotal;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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
}