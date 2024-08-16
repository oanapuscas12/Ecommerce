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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/documents-list")
    public String documentsList(@RequestParam(required = false) String role,
                                @RequestParam(value = "merchantId", required = false) Long merchantId,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size);
        String pageRole = role != null ? role : "admin";
        User currentUser = userService.getCurrentUser();
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";

        Page<User> userPage;
        if ("admin".equalsIgnoreCase(pageRole)) {
            userPage = userService.getAllAdmins(pageable);
        } else if ("merchant".equalsIgnoreCase(pageRole)) {
            userPage = userService.getAllMerchants(pageable);
        } else {
            return "redirect:/user/users?role=admin";
        }

        if (userService.isMerchant()) {
            model.addAttribute("documents", documentsService.getDocumentsForCurrentMerchant(pageable));
        } else if (userService.isAdmin()) {
            if (merchantId != null && merchantId > 0) {
                model.addAttribute("documents", documentsService.getDocumentsByMerchant(merchantId, pageable));
            } else {
                model.addAttribute("documents", documentsService.getAllDocuments(pageable));
            }
            model.addAttribute("merchants", userService.getAllMerchants(PageRequest.of(0, Integer.MAX_VALUE)));
        } else {
            return "error/403"; // Unauthorized access
        }
        model.addAttribute("isAdmin", userService.isAdmin());
        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", pageRole.substring(0, 1).toUpperCase() + pageRole.substring(1) + "s List");
        model.addAttribute("userPage", userPage);

        return "documents/documents-list";
    }


    @GetMapping("/upload-document")
    public String uploadDocument(Model model, @RequestParam(required = false) String role) {
        String pageRole = role != null ? role : "admin";
        User currentUser = userService.getCurrentUser();
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";
        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", pageRole.substring(0, 1).toUpperCase() + pageRole.substring(1) + "s List");

        model.addAttribute("pageTitle", "Upload Document");

        if (userService.isMerchant() || userService.isAdmin()) {
            model.addAttribute("isAdmin", userService.isAdmin());
            if (userService.isAdmin()) {
                model.addAttribute("users", userService.getNonAdminUsersExcluding(currentUser.getId()));
                model.addAttribute("currentUserId", currentUser.getId());
            }
            return "documents/upload-document";
        } else {
            return "error/403";
        }
    }

    @PostMapping("/upload-document")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "userId", required = false) Long userId,
                                   Model model) {
        if (!userService.isMerchant() && !userService.isAdmin()) {
            return "error/403";
        }

        model.addAttribute("isAdmin", userService.isAdmin());
        if (userService.isAdmin()) {
            User currentUser = userService.getCurrentUser();
            model.addAttribute("users", userService.getNonAdminUsersExcluding(currentUser.getId()));
            model.addAttribute("currentUserId", currentUser.getId());
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
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(document.getPath());
        Resource fileResource;
        try {
            fileResource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);
    }
}
