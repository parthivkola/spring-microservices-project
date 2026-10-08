package com.microservices.analytics.controller;

import com.microservices.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * AnalyticsController — exposes the in-memory aggregate summary.
 *
 * GET /api/analytics/summary →
 *   { "totalOrders": 3, "totalRevenue": 1299.97, "productOrderCounts": { "p1": 2, "p2": 1 } }
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}
