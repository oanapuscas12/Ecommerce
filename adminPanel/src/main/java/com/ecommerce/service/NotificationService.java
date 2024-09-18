package com.ecommerce.service;

import com.ecommerce.model.Notification;
import com.ecommerce.model.User;
import com.ecommerce.repository.NotificationRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotificationService {

    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

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
        return notificationRepository.findByUserIdAndRead(userId, false);
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
        return notificationRepository.findByUserAndIsReadFalse(user);
    }
}