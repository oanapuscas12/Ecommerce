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

@Controller
@RequestMapping("/documents")
public class DocumentsController {

    @Autowired
    private DocumentsService documentsService;

    @Autowired
    private UserService userService;

    @GetMapping("/documents-list")
    public String documentsList(
            @RequestParam(value = "merchantId", required = false) String merchantId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        User currentUser = userService.getCurrentUser();
        String pageRole = currentUser.isAdmin() ? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";

        Page<Document> documentPage;

        if (userService.isMerchant()) {
            documentPage = documentsService.getDocumentsForCurrentMerchant(pageable);
        } else if (userService.isAdmin()) {
            if (merchantId == null || merchantId.trim().isEmpty()) {
                documentPage = documentsService.getAllDocuments(pageable);
            } else {
                Long merchantIdLong = Long.parseLong(merchantId);
                documentPage = documentsService.getDocumentsByMerchant(merchantIdLong, pageable);
            }
            model.addAttribute("merchants", userService.getAllMerchants(PageRequest.of(0, Integer.MAX_VALUE)));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        model.addAttribute("documents", documentPage);
        model.addAttribute("isAdmin", userService.isAdmin());
        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Document List");
        model.addAttribute("merchantId", merchantId);
        assert documentPage != null;
        model.addAttribute("noDataAvailable", !documentPage.hasContent());

        return "documents/documents-list";
    }

    @GetMapping("/upload-document")
    public String uploadDocument(Model model) {
        User currentUser = userService.getCurrentUser();
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
    }

    @PostMapping("/upload-document")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "userId", required = false) Long userId,
                                   Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        if (!userService.isMerchant() && !userService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        model.addAttribute("isAdmin", userService.isAdmin());
        if (userService.isAdmin()) {
            model.addAttribute("users", userService.getNonAdminUsersExcluding(currentUser.getId()));
        }

        try {
            Long actualUserId = userId != null ? userId : userService.getCurrentUser().getId();
            if (documentsService.isDocumentAlreadyExists(actualUserId, file.getOriginalFilename())) {
                model.addAttribute("errorMessage", "A document with the same name already exists.");
            } else {
                documentsService.saveDocument(file, actualUserId);
                model.addAttribute("successMessage", "Document uploaded successfully");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to upload document: " + e.getMessage());
        }

        return "documents/upload-document";
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("documentId") Long documentId) {
        Document document = documentsService.getDocumentById(documentId);
        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Path filePath = Paths.get(document.getPath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource fileResource;
        try {
            fileResource = new UrlResource(filePath.toUri());
            if (!fileResource.exists() || !fileResource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);
    }

    @GetMapping("/preview/{documentId}")
    public ResponseEntity<byte[]> previewDocument(@PathVariable("documentId") Long documentId) {
        Document document = documentsService.getDocumentById(documentId);
        if (document == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document Not Found");
        }

        byte[] content = document.getContent();
        if (content == null || content.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document content is empty");
        }

        HttpHeaders headers = new HttpHeaders();
        String contentType = document.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        headers.setContentType(MediaType.parseMediaType(contentType));

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
