package com.microservices.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * ================================================================
 * PHASE 5 — ASYNC MESSAGING (Kafka analytics aggregation)
 * ================================================================
 *
 * analytics-service is another independent consumer of "order-events".
 * It aggregates simple metrics in memory:
 *   - totalOrders
 *   - totalRevenue
 *   - productOrderCounts (orders per productId)
 *
 * WHY MATERIALIZED VIEWS / AGGREGATES VIA EVENTS?
 * -----------------------------------------------
 * Instead of scanning the order DB for dashboards, you maintain a
 * pre-computed projection updated by each event. This is the idea behind
 * CQRS / event sourcing "read models" — write path and read path diverge.
 *
 * In-memory AtomicLong / ConcurrentHashMap is fine for a single-instance demo.
 * Production would use ClickHouse, Elasticsearch, Redis, or a warehouse.
 *
 * ================================================================
 * INTERVIEW Q&A
 * ================================================================
 *
 * Q1: Why not query order-service for analytics?
 * A:  Coupling + load. Heavy dashboard queries would stress the transactional
 *     order DB. An event-driven projection scales and fails independently.
 *
 * Q2: How do you keep counts accurate under concurrency?
 * A:  Use atomic types (AtomicLong) and ConcurrentHashMap.merge / compute.
 *     For multi-instance analytics, use a shared store (Redis INCR) or
 *     stream processors (Kafka Streams / Flink).
 * ================================================================
 */
@SpringBootApplication
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
