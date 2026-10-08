package com.microservices.notification.controller;

import com.microservices.notification.service.NotificationService;
import com.microservices.notification.service.NotificationService.NotificationRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * NotificationController — query notifications stored after Kafka consumption.
 *
 * GET /api/notifications              → all notifications
 * GET /api/notifications/user/{userId} → filter by user
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationRecord>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationRecord>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getByUserId(userId));
    }
}
