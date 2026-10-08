package com.microservices.product.controller;

import com.microservices.product.model.Product;
import com.microservices.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog and stock")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id (Redis-cached)")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reduce-stock")
    @Operation(summary = "Reduce stock after an order is placed (called by order-service Feign)")
    public ResponseEntity<String> reduceStock(
            @PathVariable String id,
            @RequestParam int quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.ok("Stock reduced successfully");
    }
}
