package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ReportsController {

    @Autowired
    private UserService userService;

    @GetMapping("/reports")
    public String reportsPage(Model model, @RequestParam(required = false) String role) throws JsonProcessingException {
        String pageRole = role != null ? role : "admin";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";
        User currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        int currentYear = LocalDate.now().getYear();
        Map<String, Long> monthlyEnrollments = userService.getMonthlyMerchantEnrollments(currentYear);

        List<String> months = new ArrayList<>(monthlyEnrollments.keySet());
        List<Long> enrollments = new ArrayList<>(monthlyEnrollments.values());

        ObjectMapper mapper = new ObjectMapper();
        String monthsJson = mapper.writeValueAsString(months);
        String enrollmentsJson = mapper.writeValueAsString(enrollments);

        model.addAttribute("monthsJson", monthsJson);
        model.addAttribute("enrollmentsJson", enrollmentsJson);
        model.addAttribute("role", pageRole);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Reports");
        return "reports/reports";
    }
}