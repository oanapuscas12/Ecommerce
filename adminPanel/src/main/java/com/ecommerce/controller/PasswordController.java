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

@Controller  // Marks this class as a Spring MVC controller handling HTTP requests.
public class PasswordController {

    @Autowired
    private UserService userService;  // Injects UserService to handle user-related operations like password recovery.

    @GetMapping("/recover-password")  // Maps HTTP GET requests to "/recover-password" to this method.
    public String recoverPassword() {
        // Returns the "recover-password" view where the user can submit their email to start the password recovery process.
        return "password/recover-password";
    }

    @PostMapping("/recover-password")  // Maps HTTP POST requests to "/recover-password" to this method.
    public String recoverPassword(@RequestParam String email, HttpServletRequest request, HttpServletResponse response, Model model) {
        // Initiates the password recovery process by calling the UserService with the provided email.
        boolean initiated = userService.initiatePasswordRecover(email);

        // Prepares a success or failure message based on whether the recovery process was initiated.
        String message = initiated ? "A password reset link has been sent to your email." : "The email address provided was not found.";

        if (initiated) {
            // If recovery was initiated, logs out the user by clearing the security context.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);  // Logs out the current user.
            }
        }

        // Adds feedback and status to the model to be displayed on the response page.
        model.addAttribute("message", message);
        model.addAttribute("action", "recover-password");
        model.addAttribute("success", initiated);

        // Returns a view that informs the user of the outcome of their password recovery request.
        return "/password/response";
    }

    @GetMapping("/reset-password")  // Maps HTTP GET requests to "/reset-password" to this method.
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Validates the password reset token using the UserService.
        boolean validToken = userService.validatePasswordResetToken(token);

        // Prepares a message depending on whether the token is valid or not.
        String message = validToken ? null : "Invalid or expired token.";

        // Adds token validation status and any relevant message to the model.
        model.addAttribute("token", token);
        model.addAttribute("message", message);
        model.addAttribute("action", "reset-password");
        model.addAttribute("success", validToken);

        // Returns the password reset form if the token is valid, otherwise returns an error response view.
        return validToken ? "password/reset-password" : "response";
    }

    @PostMapping("/reset-password")  // Maps HTTP POST requests to "/reset-password" to this method.
    public String resetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
        // Resets the user's password by passing the token and new password to the UserService.
        boolean success = userService.resetPassword(token, password);

        // Prepares a success or failure message based on the outcome of the password reset.
        String message = success ? "Password successfully reset. You can now log in with your new password." : "Invalid or expired token. Please request a new password reset.";

        // Adds feedback and status to the model for the user.
        model.addAttribute("message", message);
        model.addAttribute("action", "reset-password");
        model.addAttribute("success", success);

        // If successful, redirects the user to the login page. Otherwise, shows an error response view.
        return success ? "login" : "/password/response";
    }
}