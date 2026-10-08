package com.microservices.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/*
 * InventoryService — Redis-backed stock counters.
 *
 * KEY DESIGN:
 *   inventory:{productId}  →  string integer (e.g. "50")
 *
 * WHY StringRedisTemplate?
 * -----------------------
 * Redis stores everything as bytes/strings. StringRedisTemplate is the
 * Spring convenience wrapper for string keys and string values.
 * opsForValue() gives Redis string commands: GET, SET, INCR, DECRBY, etc.
 *
 * DECRBY is atomic at the Redis server — two concurrent consumers cannot
 * interleave reads/writes and corrupt the counter (unlike a naive get-then-set).
 *
 * ApplicationRunner seeds stock on startup so demos work without manual Redis setup.
 * Matches product-service seed data: p1=50, p2=200, p3=5.
 */
@Service
public class InventoryService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final String KEY_PREFIX = "inventory:";
    private static final long DEFAULT_SEED = 100L;

    private final StringRedisTemplate redisTemplate;

    public InventoryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Seeds known products so GET /api/inventory/{id} works immediately.
     * ApplicationRunner runs once after the Spring context is ready.
     */
    @Override
    public void run(ApplicationArguments args) {
        seedIfAbsent("p1", 50);
        seedIfAbsent("p2", 200);
        seedIfAbsent("p3", 5);
        log.info("Inventory seed complete (p1=50, p2=200, p3=5)");
    }

    /**
     * Reserves (decrements) stock for a product after an order event.
     * If the Redis key is missing, seeds it with 100 first, then DECRBY.
     */
    public long reserveStock(String productId, int quantity) {
        String key = KEY_PREFIX + productId;
        Boolean exists = redisTemplate.hasKey(key);
        if (exists == null || !exists) {
            log.warn("Key {} missing — seeding with default stock {}", key, DEFAULT_SEED);
            redisTemplate.opsForValue().set(key, String.valueOf(DEFAULT_SEED));
        }
        Long remaining = redisTemplate.opsForValue().decrement(key, quantity);
        log.info("Reserved {} units of {} — remaining stock={}", quantity, productId, remaining);
        return remaining != null ? remaining : 0L;
    }

    /** Returns current stock, or empty if the product key does not exist. */
    public Long getStock(String productId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + productId);
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    private void seedIfAbsent(String productId, long quantity) {
        String key = KEY_PREFIX + productId;
        Boolean exists = redisTemplate.hasKey(key);
        if (exists == null || !exists) {
            redisTemplate.opsForValue().set(key, String.valueOf(quantity));
        }
    }
}
