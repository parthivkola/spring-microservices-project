package com.microservices.inventory.kafka;

import com.microservices.inventory.event.OrderCreatedEvent;
import com.microservices.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * OrderEventConsumer — Kafka listener for the "order-events" topic.
 *
 * @KafkaListener creates a message listener container at startup.
 * Spring Kafka:
 *   1. Polls Kafka for new records in inventory-service-group
 *   2. Deserializes the value into OrderCreatedEvent (JsonDeserializer)
 *   3. Invokes this method on a listener thread
 *   4. Commits the offset after successful processing (default ack mode)
 *
 * IDEMPOTENCY NOTE (interview talking point):
 * If processing fails AFTER Redis DECRBY but BEFORE offset commit,
 * Kafka will redeliver → stock could be decremented twice.
 * Production fix: store processed orderIds (idempotency key) or use
 * Redis transactions / Lua scripts that check orderId first.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;

    public OrderEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);
        inventoryService.reserveStock(event.getProductId(), event.getQuantity());
    }
}
