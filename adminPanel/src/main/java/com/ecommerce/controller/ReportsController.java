package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportsController {

    @Autowired
    private UserService userService;

    @GetMapping("/reports")
    public String reportsPage(Model model, @RequestParam(required = false) String role) {
        String pageRole = role != null ? role : "admin";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }
        assert currentUser != null;
        model.addAttribute("role", pageRole);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Reports");
        return "reports/reports";
    }
}