package com.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/documents")
public class DocumentsController {
    @GetMapping("/documents-list")
    public String documentsList() {
        return "documents/documents-list";
    }

    @GetMapping("/upload-document")
    public String uploadDocument() {
        return "documents/upload-document";
    }
}

