package com.ecommerce.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String path;
    private LocalDateTime uploadTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User uploadedBy;

    @Transient
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Document(String name, String path, User uploadedBy, LocalDateTime uploadTime) {
        this.name = name;
        this.path = path;
        this.uploadedBy = uploadedBy;
        this.uploadTime = uploadTime;
    }

    // Method to return the formatted date string
    public String getFormattedUploadTime() {
        return this.uploadTime != null ? this.uploadTime.format(formatter) : "";
    }
}
