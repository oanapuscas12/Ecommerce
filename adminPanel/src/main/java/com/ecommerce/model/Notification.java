package com.ecommerce.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Notification {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private String message;

    private boolean isRead; // Indicates if the notification has been read

    @Getter
    @Setter
    private LocalDateTime timestamp;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id") // Explicit JoinColumn to reference user_id
    private User user;

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}