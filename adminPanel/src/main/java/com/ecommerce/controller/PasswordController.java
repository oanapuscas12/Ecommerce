package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class PasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/recover-password")
    public String recoverPassword() {
        return "password/recover-password";
    }

    @PostMapping("/recover-password")
    public String recoverPassword(@RequestParam String email, HttpServletRequest request, HttpServletResponse response, Model model) {
        boolean initiated = userService.initiatePasswordRecover(email);
        String message = initiated ? "A password reset link has been sent to your email." : "The email address provided was not found.";
        if (initiated) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
        }
        model.addAttribute("message", message);
        model.addAttribute("action", "recover-password");
        model.addAttribute("success", initiated);
        return "/password/response";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        boolean validToken = userService.validatePasswordResetToken(token);
        String message = validToken ? null : "Invalid or expired token.";
        model.addAttribute("token", token);
        model.addAttribute("message", message);
        model.addAttribute("action", "reset-password");
        model.addAttribute("success", validToken);
        return validToken ? "password/reset-password" : "response";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
        boolean success = userService.resetPassword(token, password);
        String message = success ? "Password successfully reset. You can now log in with your new password." : "Invalid or expired token. Please request a new password reset.";
        model.addAttribute("message", message);
        model.addAttribute("action", "reset-password");
        model.addAttribute("success", success);
        return success ? "login" : "/password/response";
    }
}
