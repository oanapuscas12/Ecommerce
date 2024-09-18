package com.ecommerce.controller;

import com.ecommerce.model.Feedback;
import com.ecommerce.model.User;
import com.ecommerce.service.FeedbackService;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public String showFeedbackList(Model model, @RequestParam(required = false) String role, @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.isAdmin();
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";
        Pageable pageable = PageRequest.of(page, size);
        Page<Feedback> feedbackPage = feedbackService.getAllFeedback(pageable);
        model.addAttribute("feedbackList", feedbackPage);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("role", role);
        model.addAttribute("pageTitle", "Feedback List");
        assert feedbackPage != null;
        model.addAttribute("noDataAvailable", !feedbackPage.hasContent());
        return "feedback/list";
    }

    @GetMapping("/form")
    public String showFeedbackForm(Model model, @RequestParam(required = false) String role) {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.isAdmin();
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";
        Feedback feedback = new Feedback();
        feedback.setUsername(currentUser.getUsername());
        feedback.setEmail(currentUser.getEmail());

        model.addAttribute("feedback", feedback);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("role", role);
        model.addAttribute("status", "");
        model.addAttribute("pageTitle", "Feedback Form");
        return "feedback/form";
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute Feedback feedback, Model model) {
        User currentUser = userService.getCurrentUser();
        feedback.setUsername(currentUser.getUsername());
        feedback.setEmail(currentUser.getEmail());
        model.addAttribute("currentUser", currentUser);
        try {
            feedbackService.saveFeedback(feedback);
            model.addAttribute("message", "Thank you for your feedback!");
            model.addAttribute("status", "success");
            model.addAttribute("formPageUrl", "/home");
        } catch (Exception e) {
            model.addAttribute("message", "There was an error submitting your feedback. Please try again.");
            model.addAttribute("status", "failure");
            model.addAttribute("formPageUrl", "/feedback/form");
        }
        return "success-fail-page/success-fail";
    }
}