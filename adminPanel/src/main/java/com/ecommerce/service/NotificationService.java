package com.ecommerce.service;

import com.ecommerce.controller.NotificationController;
import com.ecommerce.model.Notification;
import com.ecommerce.model.User;
import com.ecommerce.repository.NotificationRepository;
import com.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    /**
     * Creates and saves a notification for a specific user.
     * @param userId the ID of the user to receive the notification.
     * @param message the message of the notification.
     */
    public void createNotification(Long userId, String message) {
        // Găsește utilizatorul pe baza ID-ului
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Creează notificarea
        Notification notification = new Notification();
        notification.setUser(user); // setează utilizatorul
        notification.setMessage(message);
        notification.setRead(false);
        notification.setTimestamp(LocalDateTime.now());

        // Salvează notificarea
        notificationRepository.save(notification);
    }

    /**
     * Retrieves all unread notifications for a specific user.
     * @param userId the ID of the user.
     * @return a list of unread notifications.
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    /**
     * Marks a notification as read.
     * @param notificationId the ID of the notification to be marked as read.
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found with ID: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotificationsForUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    @PostConstruct
    public void init() {
        logger.info("NotificationService bean initialized");
    }
}