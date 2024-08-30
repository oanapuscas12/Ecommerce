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

    private String contentType;

    @Lob
    @Column(name = "content")
    private byte[] content;

    @Transient
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Document(String name, String path, User uploadedBy, LocalDateTime uploadTime, String contentType, byte[] content) {
        this.name = name;
        this.path = path;
        this.uploadedBy = uploadedBy;
        this.uploadTime = uploadTime;
        this.contentType = contentType;
        this.content = content;
    }

    public String getFormattedUploadTime() {
        return this.uploadTime != null ? this.uploadTime.format(formatter) : "";
    }

    // Make this method static
    private static String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return "";
        }
        return filename.substring(lastIndexOfDot + 1).toLowerCase();
    }

    public static String getMimeType(String filename) {
        String extension = getFileExtension(filename);
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            default:
                return "application/octet-stream";
        }
    }
}