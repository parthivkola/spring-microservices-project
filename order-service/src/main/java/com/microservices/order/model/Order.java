package com.microservices.order.model;

import java.time.LocalDateTime;

/*
 * Order entity — Jackson-friendly POJO (idempotency cache + API responses).
 * Status values: CONFIRMED, FAILED
 */
public class Order {

    private String id;
    private String userId;
    private String productId;
    private int quantity;
    private double totalPrice;
    private String status;
    private LocalDateTime createdAt;

    public Order() {
    }

    public Order(String id, String userId, String productId,
                 int quantity, double totalPrice, String status) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
