package com.microservices.order.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/*
 * Idempotency-Key pattern
 * -----------------------
 * Client sends header: Idempotency-Key: <uuid>
 * First request: process + store response in Redis (TTL 24h)
 * Duplicate request with same key: return cached response, do NOT create another order
 *
 * Why Redis? Fast, TTL support, shared across order-service replicas.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String KEY_PREFIX = "idempotency:order:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public IdempotencyService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public Optional<Order> findCached(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        String json = redis.opsForValue().get(KEY_PREFIX + idempotencyKey);
        if (json == null) {
            return Optional.empty();
        }
        try {
            log.info("Idempotency hit for key={}", idempotencyKey);
            return Optional.of(objectMapper.readValue(json, Order.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached order for key={}", idempotencyKey);
            return Optional.empty();
        }
    }

    public void cache(String idempotencyKey, Order order) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        try {
            redis.opsForValue().set(KEY_PREFIX + idempotencyKey,
                    objectMapper.writeValueAsString(order), TTL);
            log.info("Cached order {} under idempotency key={}", order.getId(), idempotencyKey);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache order for key={}", idempotencyKey);
        }
    }
}
