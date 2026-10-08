package com.microservices.analytics.event;

/*
 * OrderCreatedEvent — Kafka payload (POJO for Jackson JsonDeserializer).
 * Must stay field-compatible with the producer and sibling consumers.
 */
public class OrderCreatedEvent {

    private String orderId;
    private String userId;
    private String productId;
    private int quantity;
    private double totalPrice;
    private String status;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(String orderId, String userId, String productId,
                             int quantity, double totalPrice, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{orderId='" + orderId + "', userId='" + userId +
                "', productId='" + productId + "', quantity=" + quantity +
                ", totalPrice=" + totalPrice + ", status='" + status + "'}";
    }
}
