package com.microservices.analytics.service;

import com.microservices.analytics.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/*
 * AnalyticsService — thread-safe in-memory aggregates updated from Kafka.
 *
 * AtomicLong totalOrders          — cas-based increment, no locks needed
 * AtomicReference<Double> revenue — update via accumulateAndGet for concurrent adds
 * ConcurrentHashMap productOrderCounts — merge() atomically increments per product
 *
 * Note: Double is not ideal for money in production (use BigDecimal / long cents).
 * AtomicReference<Double> is used here as specified for the learning exercise.
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AtomicLong totalOrders = new AtomicLong(0);
    private final AtomicReference<Double> totalRevenue = new AtomicReference<>(0.0);
    private final ConcurrentHashMap<String, Long> productOrderCounts = new ConcurrentHashMap<>();

    public void recordOrder(OrderCreatedEvent event) {
        totalOrders.incrementAndGet();
        totalRevenue.accumulateAndGet(event.getTotalPrice(), Double::sum);
        productOrderCounts.merge(event.getProductId(), 1L, Long::sum);
        log.info("Analytics updated — totalOrders={}, totalRevenue={}, productCounts={}",
                totalOrders.get(), totalRevenue.get(), productOrderCounts);
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", totalOrders.get());
        summary.put("totalRevenue", totalRevenue.get());
        summary.put("productOrderCounts", new HashMap<>(productOrderCounts));
        return summary;
    }
}
