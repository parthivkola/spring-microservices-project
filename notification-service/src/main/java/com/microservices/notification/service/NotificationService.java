package com.microservices.notification.service;

import com.microservices.notification.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
 * NotificationService — in-memory notification store for learning demos.
 *
 * ConcurrentHashMap: Kafka listeners may process partitions in parallel;
 * a plain HashMap would race under concurrent puts.
 *
 * Real systems would persist to a DB and dispatch via email/SMS providers.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<String, NotificationRecord> notifications = new ConcurrentHashMap<>();

    public void sendNotification(OrderCreatedEvent event) {
        log.info("Sending notification to user {} for order {}",
                event.getUserId(), event.getOrderId());

        String id = UUID.randomUUID().toString();
        NotificationRecord record = new NotificationRecord(
                id,
                event.getUserId(),
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity(),
                event.getTotalPrice(),
                "ORDER_CREATED",
                Instant.now()
        );
        notifications.put(id, record);
    }

    public List<NotificationRecord> getAll() {
        return new ArrayList<>(notifications.values());
    }

    public List<NotificationRecord> getByUserId(String userId) {
        return notifications.values().stream()
                .filter(n -> userId.equals(n.userId()))
                .collect(Collectors.toList());
    }

    /**
     * Immutable notification snapshot returned by the REST API.
     */
    public record NotificationRecord(
            String id,
            String userId,
            String orderId,
            String productId,
            int quantity,
            double totalPrice,
            String type,
            Instant createdAt
    ) {
    }
}
