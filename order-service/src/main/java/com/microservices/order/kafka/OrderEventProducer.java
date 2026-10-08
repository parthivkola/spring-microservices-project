package com.microservices.order.kafka;

import com.microservices.order.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/*
 * Publishes OrderCreatedEvent to topic "order-events".
 *
 * Interview tip — dual-write problem:
 * Saving the order + publishing to Kafka are two separate operations.
 * If the process crashes after DB save but before Kafka publish, consumers miss the event.
 * Production fix: Outbox Pattern (write event to outbox table in same DB transaction).
 */
@Component
public class OrderEventProducer {

    public static final String TOPIC = "order-events";

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        // Key = orderId → same order always lands on the same partition (ordering)
        // Async send: do not block the HTTP thread waiting for broker ack.
        // On failure we log — production would use the Outbox pattern instead.
        kafkaTemplate.send(TOPIC, event.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderCreatedEvent key={}: {}",
                                event.getOrderId(), ex.getMessage());
                    } else {
                        log.info("Published OrderCreatedEvent to topic={} key={} productId={}",
                                TOPIC, event.getOrderId(), event.getProductId());
                    }
                });
    }
}
