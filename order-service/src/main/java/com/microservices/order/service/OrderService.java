package com.microservices.order.service;

import com.microservices.order.client.dto.ProductResponse;
import com.microservices.order.client.dto.UserResponse;
import com.microservices.order.event.OrderCreatedEvent;
import com.microservices.order.idempotency.IdempotencyService;
import com.microservices.order.kafka.OrderEventProducer;
import com.microservices.order.model.CreateOrderRequest;
import com.microservices.order.model.Order;
import com.microservices.order.resilience.ProductServiceGateway;
import com.microservices.order.resilience.UserServiceGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * OrderService — orchestrates sync Feign calls + async Kafka event.
 *
 * Flow:
 *   1. Idempotency check (Redis)
 *   2. Validate user (Feign + Retry)
 *   3. Check product stock (Feign + CircuitBreaker + Retry)
 *   4. Reduce stock (Feign + CircuitBreaker + Bulkhead)
 *   5. Save order
 *   6. Publish OrderCreatedEvent → inventory / notification / analytics
 *   7. Cache response under Idempotency-Key
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final UserServiceGateway userServiceGateway;
    private final ProductServiceGateway productServiceGateway;
    private final OrderEventProducer orderEventProducer;
    private final IdempotencyService idempotencyService;

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderService(UserServiceGateway userServiceGateway,
                        ProductServiceGateway productServiceGateway,
                        OrderEventProducer orderEventProducer,
                        IdempotencyService idempotencyService) {
        this.userServiceGateway = userServiceGateway;
        this.productServiceGateway = productServiceGateway;
        this.orderEventProducer = orderEventProducer;
        this.idempotencyService = idempotencyService;
    }

    public Order createOrder(CreateOrderRequest request, String idempotencyKey) {
        Optional<Order> cached = idempotencyService.findCached(idempotencyKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        log.info("Creating order: userId={}, productId={}, quantity={}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        UserResponse user = userServiceGateway.getUser(request.getUserId());
        log.info("User validated: {}", user.getName());

        ProductResponse product = productServiceGateway.getProduct(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Requested: " + request.getQuantity() +
                    ", Available: " + product.getStock());
        }
        log.info("Product validated: {} | price={} | stock={}",
                product.getName(), product.getPrice(), product.getStock());

        productServiceGateway.reduceStock(request.getProductId(), request.getQuantity());

        double totalPrice = product.getPrice() * request.getQuantity();
        Order order = new Order(
                UUID.randomUUID().toString(),
                request.getUserId(),
                request.getProductId(),
                request.getQuantity(),
                totalPrice,
                "CONFIRMED"
        );
        orders.put(order.getId(), order);

        orderEventProducer.publishOrderCreated(new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus()
        ));

        idempotencyService.cache(idempotencyKey, order);
        log.info("Order created: id={}, total={}", order.getId(), order.getTotalPrice());
        return order;
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }
}
