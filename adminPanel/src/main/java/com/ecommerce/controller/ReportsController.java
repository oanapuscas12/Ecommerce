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
import java.util.stream.Collectors;

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

        // Data for Enrollment Merchants/Months Overview
        Map<String, Long> monthlyMerchantsEnrollments = userService.getAllMonthlyMerchantEnrollments(currentYear);

        List<String> monthsForMerchants = new ArrayList<>(monthlyMerchantsEnrollments.keySet());
        List<Long> enrollmentsForMerchants = new ArrayList<>(monthlyMerchantsEnrollments.values());

        ObjectMapper mapper = new ObjectMapper();
        String monthsForMerchantsJson = mapper.writeValueAsString(monthsForMerchants);
        String merchantsEnrollmentsForMerchantsJson = mapper.writeValueAsString(enrollmentsForMerchants);

        model.addAttribute("monthsForMerchantsJson", monthsForMerchantsJson);
        model.addAttribute("merchantsEnrollmentsForMerchantsJson", merchantsEnrollmentsForMerchantsJson);

        // Data for Enrollment Admins/Months Overview
        Map<String, Long> monthlyAdminsEnrollments = userService.getAllMonthlyAdminEnrollmentPercentages(currentYear);

        List<String> monthsForAdmins = new ArrayList<>(monthlyAdminsEnrollments.keySet());
        List<Long> enrollmentsForAdmins = new ArrayList<>(monthlyAdminsEnrollments.values());

        String monthsForAdminsJson = mapper.writeValueAsString(monthsForAdmins);
        System.out.println(monthsForAdminsJson);
        String merchantsEnrollmentsForAdminsJson = mapper.writeValueAsString(enrollmentsForAdmins);
        System.out.println(merchantsEnrollmentsForAdminsJson);


        model.addAttribute("monthsForAdminsJson", monthsForAdminsJson);
        model.addAttribute("merchantsEnrollmentsForAdminsJson", merchantsEnrollmentsForAdminsJson);

        model.addAttribute("role", pageRole);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Reports");
        return "reports/reports";
    }
}
