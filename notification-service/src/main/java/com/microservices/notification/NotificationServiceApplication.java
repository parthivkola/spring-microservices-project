package com.microservices.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * ================================================================
 * PHASE 5 — ASYNC MESSAGING (Kafka notification fan-out)
 * ================================================================
 *
 * notification-service demonstrates the classic pub-sub pattern:
 * ONE producer (order-service) → MANY independent consumers.
 *
 * This service's job is intentionally simple for learning:
 *   - Consume OrderCreatedEvent
 *   - Log "Sending notification to user X for order Y"
 *   - Store the notification in an in-memory ConcurrentHashMap
 *
 * In production you would call email/SMS/push providers (SendGrid, SES, FCM).
 * Keeping it in-memory keeps the learning focus on Kafka consumer groups.
 *
 * WHY A SEPARATE CONSUMER GROUP?
 * ------------------------------
 * group-id = notification-service-group
 * inventory-service uses inventory-service-group.
 * Because the groups differ, BOTH services receive EVERY message on
 * "order-events". Same group ID would mean competing consumers (load share).
 *
 * ================================================================
 * INTERVIEW Q&A
 * ================================================================
 *
 * Q1: How do multiple services all react to the same order event?
 * A:  Publish once to a Kafka topic. Each interested service uses its own
 *     consumer group. Kafka delivers a copy of every message to each group.
 *
 * Q2: Sync notification vs async?
 * A:  Sync (call email API inside order-service) adds latency and couples
 *     order success to the email provider. Async (Kafka) lets order-service
 *     return immediately; notifications can retry/fail independently.
 * ================================================================
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
