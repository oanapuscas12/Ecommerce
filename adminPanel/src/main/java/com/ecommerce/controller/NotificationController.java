package com.ecommerce.controller;

import com.ecommerce.model.Notification;
import com.ecommerce.model.User;
import com.ecommerce.service.NotificationService;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications() {
        // Check if notificationService is null and log an error if it is
        if (notificationService == null) {
            logger.error("NotificationService is null");
            throw new IllegalStateException("NotificationService is not initialized");
        }

        User currentUser = userService.getCurrentUser();

        // Optional: Check if currentUser is null and handle accordingly
        if (currentUser == null) {
            logger.error("Current user is null");
            throw new IllegalStateException("Current user is not authenticated");
        }

        return notificationService.getUnreadNotificationsForUser(currentUser);
    }
}