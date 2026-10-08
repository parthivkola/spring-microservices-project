package com.microservices.notification.kafka;

import com.microservices.notification.event.OrderCreatedEvent;
import com.microservices.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * Kafka listener for "order-events".
 * groupId must match spring.kafka.consumer.group-id so offsets are tracked
 * under notification-service-group (independent of inventory/analytics).
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);
        notificationService.sendNotification(event);
    }
}
