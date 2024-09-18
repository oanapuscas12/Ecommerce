package com.ecommerce.repository;

import com.ecommerce.model.Notification;
import com.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndIsRead(Long userId, boolean isRead);

    // Query to obtain unread notifications for a specific user
    List<Notification> findByUserAndIsReadFalse(User user);

    List<Notification> findByUserAndIsReadTrue(User user);
}