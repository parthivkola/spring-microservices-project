package com.microservices.product.service;

import com.microservices.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final Map<String, Product> products = new ConcurrentHashMap<>(Map.of(
            "p1", new Product("p1", "Laptop", 999.99, 50),
            "p2", new Product("p2", "Headphones", 149.99, 200),
            "p3", new Product("p3", "Mouse", 29.99, 5)
    ));

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(getProduct(id));
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProduct(String id) {
        log.info("Cache MISS for product {} — loading from store", id);
        return products.get(id);
    }

    @CacheEvict(value = "products", key = "#productId")
    public synchronized void reduceStock(String productId, int quantity) {
        Product product = products.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock for " + product.getName() +
                    ". Requested: " + quantity + ", Available: " + product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        log.info("Stock reduced for {} → {}", productId, product.getStock());
    }
}
