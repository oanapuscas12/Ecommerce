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
        String role = isAdmin ? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";

        // Data for Enrollment Merchants for the Current Month
        Map<String, Long> monthlyMerchantsEnrollments = userService.getMerchantActivityForCurrentMonth();

        List<String> monthsForMerchants = new ArrayList<>(monthlyMerchantsEnrollments.keySet());
        List<Long> enrollmentsForMerchants = new ArrayList<>(monthlyMerchantsEnrollments.values());

        // Data for Merchants by County (unchanged as it is not month-specific)
        Map<String, Long> countyMerchantCount = userService.getMerchantCountByCounty();
        List<String> counties = new ArrayList<>(countyMerchantCount.keySet());
        List<Long> merchantCounts = new ArrayList<>(countyMerchantCount.values());

        // Data for Merchant Activity for the Current Month
        Map<String, Long> monthlyActivity = userService.getMerchantActivityForCurrentMonth();

        // Prepare data for current month activity
        List<String> activityMonths = new ArrayList<>(monthlyActivity.keySet());
        List<Long> newMerchants = new ArrayList<>();
        List<Long> returningMerchants = new ArrayList<>();
        List<Long> inactiveMerchants = new ArrayList<>();

        // Note: This should be done for the current month only
        newMerchants.add(monthlyActivity.getOrDefault("New", 0L));
        returningMerchants.add(monthlyActivity.getOrDefault("Returning", 0L));
        inactiveMerchants.add(monthlyActivity.getOrDefault("Inactive", 0L));


        // Serialize data for the view
        ObjectMapper mapper = new ObjectMapper();
        String monthsForMerchantsJson = mapper.writeValueAsString(monthsForMerchants);
        String merchantsEnrollmentsForMerchantsJson = mapper.writeValueAsString(enrollmentsForMerchants);
        String countiesJson = mapper.writeValueAsString(counties);
        String merchantCountsJson = mapper.writeValueAsString(merchantCounts);

        String activityMonthsJson = mapper.writeValueAsString(activityMonths);
        String newMerchantsJson = mapper.writeValueAsString(newMerchants);
        String returningMerchantsJson = mapper.writeValueAsString(returningMerchants);
        String inactiveMerchantsJson = mapper.writeValueAsString(inactiveMerchants);

        // Add attributes to the model
        model.addAttribute("monthsForMerchantsJson", monthsForMerchantsJson);
        model.addAttribute("merchantsEnrollmentsForMerchantsJson", merchantsEnrollmentsForMerchantsJson);
        model.addAttribute("countiesJson", countiesJson);
        model.addAttribute("merchantCountsJson", merchantCountsJson);
        model.addAttribute("activityMonthsJson", activityMonthsJson);
        model.addAttribute("newMerchantsJson", newMerchantsJson);
        model.addAttribute("returningMerchantsJson", returningMerchantsJson);
        model.addAttribute("inactiveMerchantsJson", inactiveMerchantsJson);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("role", role);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "View Reports");
        return "reports/reports";
    }
}
