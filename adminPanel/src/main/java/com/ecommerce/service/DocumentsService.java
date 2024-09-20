package com.ecommerce.service;

import com.ecommerce.model.Document;
import com.ecommerce.model.User;
import com.ecommerce.repository.DocumentRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DocumentsService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserService userService;

    private final Path rootLocation = Paths.get("uploaded-documents");

    private static final Logger logger = LoggerFactory.getLogger(DocumentsService.class);

    public DocumentsService() {
        try {
            Files.createDirectories(rootLocation);
            logger.info("Document storage directory initialized at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not initialize storage directory", e);
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Transactional
    public void saveDocument(MultipartFile file, Long userId) throws IOException {
        User user = userService.getUserById(userId).orElseThrow(() -> new IOException("User not found"));

        String filename = sanitizeFilename(file.getOriginalFilename());
        if (filename == null || filename.isEmpty()) {
            throw new IOException("Filename is null or empty");
        }

        Path destinationFile = resolveFilePath(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File {} successfully saved to {}", filename, destinationFile);
        } catch (IOException e) {
            logger.error("Error saving file: {}", filename, e);
            throw new IOException("Error saving file", e);
        }

        String contentType = determineContentType(file, filename);

        Document document = new Document(filename, destinationFile.toString(), user, LocalDateTime.now(), contentType);

        documentRepository.save(document);
        logger.info("Document metadata saved for file: {}", filename);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private Path resolveFilePath(String filename) throws IOException {
        Path destinationFile = rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside the current directory.");
        }
        return destinationFile;
    }

    private String determineContentType(MultipartFile file, String filename) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = getMimeType(filename);
        }
        return contentType;
    }

    private String getMimeType(String filename) {
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

    private String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return "";
        }
        return filename.substring(lastIndexOfDot + 1).toLowerCase();
    }

    @NonNull
    public Page<Document> getDocumentsForCurrentMerchant(@NonNull Pageable pageable) {
        Long currentUserId = userService.getCurrentUser().getId();
        return documentRepository.findByUploadedById(currentUserId, pageable);
    }

    @Nullable
    public Page<Document> getDocumentsByMerchant(@NonNull Long merchantId, @NonNull Pageable pageable) {
        return documentRepository.findByUploadedById(merchantId, pageable);
    }

    public List<User> getMerchantsWithDocuments() {
        return documentRepository.findDistinctMerchantsWithDocuments();
    }

    @NonNull
    public Page<Document> getAllDocuments(@NonNull Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document not found with ID: " + documentId));
    }


    public boolean isDocumentAlreadyExists(Long userId, String filename) {
        logger.debug("Checking if document already exists for user ID: {} and filename: {}", userId, filename);
        return documentRepository.findByNameAndUploadedById(filename, userId).isPresent();
    }
}