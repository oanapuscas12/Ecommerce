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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DocumentsService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserService userService;

    private final Path rootLocation = Paths.get("uploaded-documents");

    public DocumentsService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public void saveDocument(MultipartFile file, Long userId) throws IOException {
        Optional<User> userOptional = userService.getUserById(userId);
        User user = userOptional.orElseThrow(() -> new IOException("User not found"));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IOException("Filename is null or empty");
        }

        filename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");

        Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                .normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new IOException("Cannot store file outside current directory.");
        }

        byte[] content = file.getBytes();

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = Document.getMimeType(filename);
        }

        Document document = documentRepository.findByNameAndUploadedById(filename, userId)
                .orElse(new Document(filename, destinationFile.toString(), user, LocalDateTime.now(), contentType, content));

        documentRepository.save(document);
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

    @NonNull
    public Page<Document> getAllDocuments(@NonNull Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId).orElse(null);
    }

    public boolean isDocumentAlreadyExists(Long userId, String filename) {
        return documentRepository.findByNameAndUploadedById(filename, userId).isPresent();
    }
}
