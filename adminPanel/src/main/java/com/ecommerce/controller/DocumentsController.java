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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/documents")
public class DocumentsController {

    @Autowired
    private DocumentsService documentsService;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(DocumentsController.class);


    @GetMapping("/documents-list")
    public String documentsList(
            @RequestParam(value = "merchantId", required = false) String merchantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        logger.info("Fetching documents list - page: {}, size: {}, merchantId: {}", page, size, merchantId);

        Pageable pageable = PageRequest.of(page, size);
        User currentUser = userService.getCurrentUser();
        logger.debug("Current user: {}", currentUser.getUsername());

        String pageRole = currentUser.isAdmin() ? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";

        Page<Document> documentPage;

        try {
            if (userService.isMerchant()) {
                documentPage = documentsService.getDocumentsForCurrentMerchant(pageable);
                logger.debug("Fetched documents for merchant");
            } else if (userService.isAdmin()) {
                if (merchantId == null || merchantId.trim().isEmpty()) {
                    documentPage = documentsService.getAllDocuments(pageable);
                    logger.debug("Fetched all documents for admin");
                } else {
                    Long merchantIdLong = Long.parseLong(merchantId);
                    documentPage = documentsService.getDocumentsByMerchant(merchantIdLong, pageable);
                    logger.debug("Fetched documents for merchant ID: {}", merchantIdLong);
                }
                model.addAttribute("merchants", userService.getAllMerchants(PageRequest.of(0, Integer.MAX_VALUE)));
            } else {
                logger.warn("Access denied for user: {}", currentUser.getUsername());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
            }
        } catch (Exception e) {
            logger.error("Error fetching documents list", e);
            throw e;
        }

        model.addAttribute("documents", documentPage);
        model.addAttribute("isAdmin", userService.isAdmin());
        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Document List");
        model.addAttribute("merchantId", merchantId);
        model.addAttribute("noDataAvailable", !documentPage.hasContent());

        logger.info("Documents list fetched successfully for user: {}", currentUser.getUsername());
        return "documents/documents-list";
    }

    @GetMapping("/upload-document")
    public String uploadDocument(Model model) {
        User currentUser = userService.getCurrentUser();
        logger.info("Navigating to upload document page for user: {}", currentUser.getUsername());

        String pageRole = currentUser.isAdmin() ? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("role", pageRole);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "Upload Document");

        if (userService.isMerchant() || userService.isAdmin()) {
            model.addAttribute("isAdmin", userService.isAdmin());
            if (userService.isAdmin()) {
                model.addAttribute("users", userService.getNonAdminUsersExcluding(currentUser.getId()));
            }
            return "documents/upload-document";
        } else {
            logger.warn("Access denied for user: {}", currentUser.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
    }

    @PostMapping("/upload-document")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "userId", required = false) Long userId,
                                   Model model) {
        User currentUser = userService.getCurrentUser();
        logger.info("Handling file upload for user: {}", currentUser.getUsername());

        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        if (!userService.isMerchant() && !userService.isAdmin()) {
            logger.warn("Access denied for file upload by user: {}", currentUser.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        model.addAttribute("isAdmin", userService.isAdmin());
        if (userService.isAdmin()) {
            model.addAttribute("users", userService.getNonAdminUsersExcluding(currentUser.getId()));
        }

        try {
            Long actualUserId = userId != null ? userId : userService.getCurrentUser().getId();
            logger.debug("Checking if document already exists for user ID: {}", actualUserId);

            if (documentsService.isDocumentAlreadyExists(actualUserId, file.getOriginalFilename())) {
                logger.warn("Document already exists: {}", file.getOriginalFilename());
                model.addAttribute("errorMessage", "A document with the same name already exists.");
            } else {
                documentsService.saveDocument(file, actualUserId);
                logger.info("Document uploaded successfully: {}", file.getOriginalFilename());
                model.addAttribute("successMessage", "Document uploaded successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to upload document", e);
            model.addAttribute("errorMessage", "Failed to upload document: " + e.getMessage());
        }

        return "documents/upload-document";
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("documentId") Long documentId) {
        logger.info("Downloading document with ID: {}", documentId);

        Document document = documentsService.getDocumentById(documentId);
        if (document == null) {
            logger.error("Document not found with ID: {}", documentId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document Not Found");
        }

        Path filePath = Paths.get(document.getPath());
        Resource fileResource;
        try {
            fileResource = new UrlResource(filePath.toUri());
            logger.info("Document found and ready for download: {}", document.getName());
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for document path: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);
    }
}
