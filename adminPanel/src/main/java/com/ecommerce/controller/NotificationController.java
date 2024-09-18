package com.ecommerce.controller;

import com.ecommerce.model.Notification;
import com.ecommerce.model.User;
import com.ecommerce.service.NotificationService;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications() {
        User currentUser = userService.getCurrentUser();
        return notificationService.getUnreadNotificationsForUser(currentUser);
    }
}