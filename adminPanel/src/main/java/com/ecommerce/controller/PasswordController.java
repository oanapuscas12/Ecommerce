package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class PasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        return "password/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, Model model) {
        boolean success = userService.initiatePasswordReset(email);
        if (success) {
            model.addAttribute("message", "A password reset link has been sent to your email.");
        } else {
            model.addAttribute("message", "No account found with that email.");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "password/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
        boolean success = userService.resetPassword(token, password);
        if (success) {
            model.addAttribute("message", "Password successfully reset.");
            return "login";
        } else {
            model.addAttribute("message", "Invalid or expired token.");
            return "password/reset-password";
        }
    }

    @GetMapping("/recover-password/{id}")
    public String recoverPasswordForm(@PathVariable Long id,@RequestParam(required = false) String role, Model model) {
        Optional<User> user = userService.getUserById(id);
        User currentUser = userService.getCurrentUser();
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role != null ? role : "admin");
            model.addAttribute("otherRole", user.get().isAdmin() ? "Admin" : "Merchant");
            model.addAttribute("pageTitle", "User Details: " + user.get().getUsername());
            return "password/recover-password";
        }
        return "redirect:/user/users";
    }

    @PostMapping("/recover-password")
    public String recoverPassword(@RequestParam Long id, @RequestParam String newPassword, @RequestParam String role) {
        userService.updatePassword(id, newPassword);
        return "redirect:/user/users?role=" + role;
    }
}
