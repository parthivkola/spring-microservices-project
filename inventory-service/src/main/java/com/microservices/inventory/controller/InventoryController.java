package com.microservices.inventory.controller;

import com.microservices.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * InventoryController — read-only API for current Redis stock levels.
 *
 * GET /api/inventory/{productId} → { "productId": "p1", "stock": 50 }
 *
 * Stock is mutated asynchronously by the Kafka consumer (reserveStock),
 * not by this controller — that keeps the HTTP API and event path separate
 * (CQRS-lite: write via events, read via REST).
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable String productId) {
        Long stock = inventoryService.getStock(productId);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "productId", productId,
                "stock", stock
        ));
    }
}
