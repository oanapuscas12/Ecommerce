package com.ecommerce.controller;

import com.ecommerce.model.Document;
import com.ecommerce.model.User;
import com.ecommerce.service.DocumentsService;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Controller  // Marks this class as a Spring MVC controller.
@RequestMapping("/documents")  // Specifies the base URL for all routes in this controller.
public class DocumentsController {

    @Autowired
    private DocumentsService documentsService;  // Injects the DocumentsService for handling document-related operations.

    @Autowired
    private UserService userService;  // Injects the UserService to manage user-related operations.

    @GetMapping("/documents-list")
    public String documentsList(
            @RequestParam(value = "merchantId", required = false) String merchantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        // Retrieves the list of documents, paginated. If merchantId is provided, filters by that merchant.

        Pageable pageable = PageRequest.of(page, size);  // Creates a pageable object for pagination.
        User currentUser = userService.getCurrentUser();  // Fetches the currently logged-in user.
        String pageRole = currentUser.isAdmin() ? "admin" : "merchant";  // Determines the user role.
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";  // Determines the opposite role.

        Page<Document> documentPage;  // Stores the paginated documents.

        if (userService.isMerchant()) {
            // If the user is a merchant, fetch documents specific to this merchant.
            documentPage = documentsService.getDocumentsForCurrentMerchant(pageable);
        } else if (userService.isAdmin()) {
            // If the user is an admin, either fetch all documents or filter by merchantId.
            if (merchantId == null || merchantId.trim().isEmpty()) {
                documentPage = documentsService.getAllDocuments(pageable);
            } else {
                Long merchantIdLong = Long.parseLong(merchantId);
                documentPage = documentsService.getDocumentsByMerchant(merchantIdLong, pageable);
            }
            model.addAttribute("merchants", userService.getAllMerchants(PageRequest.of(0, Integer.MAX_VALUE)));  // Adds merchant list for filtering.
        } else {
            // Throws an error if the user does not have access.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        // Adds various attributes to the model for rendering in the view.
        model.addAttribute("documents", documentPage);
        model.addAttribute("isAdmin", userService.isAdmin());
        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Document List");
        model.addAttribute("merchantId", merchantId);
        assert documentPage != null;
        model.addAttribute("noDataAvailable", !documentPage.hasContent());  // If no content is found, sets no data flag.

        return "documents/documents-list";  // Returns the view name for document listing.
    }

    @GetMapping("/upload-document")
    public String uploadDocument(Model model) {
        // Prepares the upload document form for the user.

        User currentUser = userService.getCurrentUser();  // Gets the current user.
        String pageRole = currentUser.isAdmin() ? "admin" : "merchant";  // Determines the user role.
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";  // Determines the opposite role.

        // Adds necessary attributes to the model.
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("role", pageRole);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "Upload Document");

        if (userService.isMerchant() || userService.isAdmin()) {
            // If the user is an admin, provide the list of users for document association.
            model.addAttribute("isAdmin", userService.isAdmin());
            if (userService.isAdmin()) {
                model.addAttribute("users", userService.getNonAdminUsersExcluding(currentUser.getId()));
            }
            return "documents/upload-document";  // Returns the upload document form view.
        } else {
            // Throws error if the user does not have permission to upload.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
    }

    @PostMapping("/upload-document")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "userId", required = false) Long userId,
                                   Model model) {
        // Handles file upload and associates it with a user.

        User currentUser = userService.getCurrentUser();  // Gets the current user.
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        if (!userService.isMerchant() && !userService.isAdmin()) {
            // If the user is neither merchant nor admin, throw forbidden error.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        // Add the necessary attributes for admin.
        model.addAttribute("isAdmin", userService.isAdmin());
        if (userService.isAdmin()) {
            model.addAttribute("users", userService.getNonAdminUsersExcluding(currentUser.getId()));
        }

        try {
            // Determine the actual user ID for the document association.
            Long actualUserId = userId != null ? userId : userService.getCurrentUser().getId();

            // Check if a document with the same name already exists.
            if (documentsService.isDocumentAlreadyExists(actualUserId, file.getOriginalFilename())) {
                model.addAttribute("errorMessage", "A document with the same name already exists.");
            } else {
                // Save the document and display success message.
                documentsService.saveDocument(file, actualUserId);
                model.addAttribute("successMessage", "Document uploaded successfully");
            }
        } catch (Exception e) {
            // Handle any exceptions during file upload.
            model.addAttribute("errorMessage", "Failed to upload document: " + e.getMessage());
        }

        return "documents/upload-document";  // Return the upload document view with result messages.
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("documentId") Long documentId) {
        // Handles document download by document ID.

        Document document = documentsService.getDocumentById(documentId);  // Fetch the document by ID.
        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // If not found, return 404.
        }

        Path filePath = Paths.get(document.getPath());  // Get the file path for the document.
        if (!Files.exists(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);  // Return 404 if the file does not exist.
        }

        Resource fileResource;
        try {
            fileResource = new UrlResource(filePath.toUri());  // Create a resource for the file.
            if (!fileResource.exists() || !fileResource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // Return error if URL is malformed.
        }

        // Set the response headers for file download.
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);  // Return the file as a download.
    }

    @GetMapping("/preview/{documentId}")
    public ResponseEntity<byte[]> previewDocument(@PathVariable("documentId") Long documentId) {
        // Handles document preview by document ID.

        Document document = documentsService.getDocumentById(documentId);  // Fetch the document by ID.
        if (document == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document Not Found");  // Throw 404 if not found.
        }

        byte[] content = document.getContent();  // Get the content of the document.
        if (content == null || content.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document content is empty");  // Throw 404 if content is empty.
        }

        HttpHeaders headers = new HttpHeaders();
        String contentType = document.getContentType();  // Get the content type.
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        headers.setContentType(MediaType.parseMediaType(contentType));  // Set the content type in response.

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);  // Return the document content for preview.
    }
}