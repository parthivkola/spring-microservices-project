package com.microservices.inventory.event;

/*
 * OrderCreatedEvent — the Kafka message payload published by order-service.
 *
 * WHY A POJO (not a record)?
 * --------------------------
 * Jackson's JsonDeserializer needs:
 *   1. A no-args constructor (to instantiate before setting fields)
 *   2. Setters (or @JsonProperty on a constructor) to populate fields
 * Java records work with Jackson 2.12+, but POJOs are the safest default
 * for Spring Kafka JsonDeserializer demos and interview answers.
 *
 * EVENT-DRIVEN CONTRACT:
 * ----------------------
 * This class is a SHARED CONTRACT between producer and consumers.
 * In production you'd put it in a common library (or use Avro/Protobuf schema registry).
 * Here each service has its own copy — simpler for a learning project, but
 * be careful: field names/types must stay in sync across services.
 *
 * Fields mirror the Order entity created in order-service.
 */
public class OrderCreatedEvent {

    private String orderId;
    private String userId;
    private String productId;
    private int quantity;
    private double totalPrice;
    private String status;

    /** Required by Jackson JsonDeserializer */
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
