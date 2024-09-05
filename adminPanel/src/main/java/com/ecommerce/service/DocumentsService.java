package com.ecommerce.service;

import com.ecommerce.model.Document;
import com.ecommerce.model.User;
import com.ecommerce.repository.DocumentRepository;
import lombok.NonNull;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;

@Service  // Marks the class as a Spring service component.
public class DocumentsService {

    @Autowired
    private DocumentRepository documentRepository;  // Repository for document data.

    @Autowired
    private UserService userService;  // Service to fetch user information.

    private final Path rootLocation = Paths.get("uploaded-documents");  // Directory for storing uploaded documents.

    private static final Logger logger = LoggerFactory.getLogger(DocumentsService.class);

    public DocumentsService() {
        try {
            Files.createDirectories(rootLocation);  // Create the directory if it does not exist.
            logger.info("Document storage directory initialized at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not initialize storage directory", e);
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public void saveDocument(MultipartFile file, Long userId) throws IOException {
        logger.info("Saving document for user ID: {}", userId);

        Optional<User> userOptional = userService.getUserById(userId);
        User user = userOptional.orElseThrow(() -> new IOException("User not found"));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IOException("Filename is null or empty");
        }

        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");  // Sanitize filename to prevent directory traversal attacks.
        logger.debug("Sanitized filename: {}", filename);

        Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                .normalize().toAbsolutePath();

        // Ensure that the file is not stored outside the designated directory.
        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory.");
        }

        byte[] content = file.getBytes();

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);  // Save the file.
            logger.info("File saved successfully: {}", destinationFile);
        } catch (IOException e) {
            logger.error("Error saving file: {}", filename, e);
            throw e;
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = Document.getMimeType(filename);  // Fallback to a method for determining MIME type if not provided.
        }

        Document document = documentRepository.findByNameAndUploadedById(filename, userId)
                .orElse(new Document(filename, destinationFile.toString(), user, LocalDateTime.now(), contentType, content));

        documentRepository.save(document);  // Save the document metadata to the database.
        logger.info("Document metadata saved to database: {}", filename);
    }

    @NonNull
    public Page<Document> getDocumentsForCurrentMerchant(@NonNull Pageable pageable) {
        Long currentUserId = userService.getCurrentUser().getId();  // Get the current user's ID.
        return documentRepository.findByUploadedById(currentUserId, pageable);  // Retrieve documents for the current user.
    }

    @Nullable
    public Page<Document> getDocumentsByMerchant(@NonNull Long merchantId, @NonNull Pageable pageable) {
        return documentRepository.findByUploadedById(merchantId, pageable);  // Retrieve documents for a specific merchant.
    }

    @NonNull
    public Page<Document> getAllDocuments(@NonNull Pageable pageable) {
        return documentRepository.findAll(pageable);  // Retrieve all documents.
    }

    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId).orElse(null);  // Retrieve a document by its ID.
    }

    public boolean isDocumentAlreadyExists(Long userId, String filename) {
        logger.debug("Checking if document already exists for user ID: {} and filename: {}", userId, filename);
        return documentRepository.findByNameAndUploadedById(filename, userId).isPresent();  // Check if a document with the same name already exists for the user.
    }
}