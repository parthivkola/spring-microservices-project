package com.microservices.analytics.kafka;

import com.microservices.analytics.event.OrderCreatedEvent;
import com.microservices.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * Consumes OrderCreatedEvent and updates in-memory analytics aggregates.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final AnalyticsService analyticsService;

    public OrderEventConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = "order-events", groupId = "analytics-service-group")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event);
        analyticsService.recordOrder(event);
    }
}
