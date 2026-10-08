package com.microservices.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * ================================================================
 * PHASE 5 — ASYNC MESSAGING (Kafka + Redis inventory)
 * ================================================================
 *
 * inventory-service is a KAFKA CONSUMER. It does NOT expose create-order
 * endpoints — it reacts asynchronously when order-service publishes an
 * OrderCreatedEvent to the "order-events" topic.
 *
 * WHY ASYNC INVENTORY?
 * --------------------
 * Synchronous stock reduction (Phase 3/4 Feign call to product-service)
 * couples order creation to inventory availability at request time.
 * With Kafka:
 *   1. order-service publishes OrderCreatedEvent and returns quickly
 *   2. inventory-service, notification-service, analytics-service ALL
 *      independently consume the SAME event (fan-out / pub-sub)
 *   3. If inventory-service is briefly down, Kafka retains the message
 *      and delivers it when the consumer comes back (durability)
 *
 * CONSUMER GROUP:
 * ---------------
 * group-id = inventory-service-group
 * Within a group, each partition is assigned to exactly ONE consumer instance.
 * Scaling: run 2 inventory-service pods → Kafka load-balances partitions
 * between them (no duplicate processing within the group).
 * Different groups (notification, analytics) each get a FULL copy of every message.
 *
 * REDIS FOR INVENTORY:
 * --------------------
 * Redis DECRBY is atomic — perfect for concurrent stock updates.
 * Key pattern: inventory:{productId}  e.g. inventory:p1 → "50"
 * Phase 8 may move authoritative stock to PostgreSQL; Redis stays as a cache.
 *
 * ================================================================
 * INTERVIEW Q&A
 * ================================================================
 *
 * Q1: Kafka topic vs consumer group?
 * A:  Topic = stream of messages (logical channel). Consumer group = set of
 *     consumers that share the work of reading a topic. Each message is
 *     delivered to ONE consumer PER group, but ALL groups receive every message.
 *
 * Q2: What is auto-offset-reset: earliest?
 * A:  When a new consumer group has no committed offset, start from the
 *     beginning of the topic (replay history). "latest" skips to new messages only.
 *
 * Q3: Why Redis for stock counters?
 * A:  In-memory atomic operations (DECRBY) with microsecond latency. Ideal for
 *     hot counters. Persistence via RDB/AOF if durability is needed.
 * ================================================================
 */
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
