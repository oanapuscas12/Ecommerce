package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ReportsController {

    @Autowired
    private UserService userService;

    @GetMapping("/reports")
    public String reportsPage(Model model) throws JsonProcessingException {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.isAdmin();
        String role = currentUser.isAdmin()? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";

        int currentYear = LocalDate.now().getYear();

        // Data for Enrollment Merchants/Months Overview
        Map<String, Long> monthlyMerchantsEnrollments = userService.getAllMonthlyMerchantEnrollments(currentYear);

        List<String> monthsForMerchants = new ArrayList<>(monthlyMerchantsEnrollments.keySet());
        List<Long> enrollmentsForMerchants = new ArrayList<>(monthlyMerchantsEnrollments.values());

        ObjectMapper mapper = new ObjectMapper();
        String monthsForMerchantsJson = mapper.writeValueAsString(monthsForMerchants);
        String merchantsEnrollmentsForMerchantsJson = mapper.writeValueAsString(enrollmentsForMerchants);

        model.addAttribute("monthsForMerchantsJson", monthsForMerchantsJson);
        model.addAttribute("merchantsEnrollmentsForMerchantsJson", merchantsEnrollmentsForMerchantsJson);

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("role", role);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Reports");
        return "reports/reports";
    }
}
