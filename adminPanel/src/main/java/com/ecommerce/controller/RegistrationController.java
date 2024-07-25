package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        boolean success = false;
        String message;
        try {
            userService.createUser(user);
            success = true;
            message = "Registration successful. Please check your email for verification.";
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
            model.addAttribute("errorMsg", e.getMessage());
        }
        model.addAttribute("message", message);
        model.addAttribute("action", "register");
        model.addAttribute("success", success);
        return "/password/response";
    }

    @GetMapping("/confirm-account")
    public String confirmAccount(@RequestParam("token") String token, Model model) {
        boolean confirmed = userService.confirmUserAccount(token);
        if (confirmed) {
            model.addAttribute("message", "Your account has been successfully confirmed.");
            return "/password/response";
        } else {
            model.addAttribute("message", "The confirmation link is invalid or expired.");
            return "/password/response";
        }
    }
}
