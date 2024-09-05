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

@Controller  // Marks this class as a Spring MVC controller, allowing it to handle HTTP requests.
public class RegistrationController {

    @Autowired
    private UserService userService;  // Injects the UserService to handle user-related operations, such as registration and account confirmation.

    @GetMapping("/register")  // Maps HTTP GET requests to "/register" to this method.
    public String showRegistrationForm(Model model) {
        // Adds a new User object to the model for form binding, to capture user registration details.
        model.addAttribute("user", new User());

        // Returns the registration form view ("register") where the user can input their details.
        return "register";
    }

    @PostMapping("/register")  // Maps HTTP POST requests to "/register" to handle the user registration process.
    public String registerUser(@ModelAttribute User user, Model model) {
        // Registers a new user and provides feedback on success or failure.
        boolean success = false;
        String message;

        try {
            // Attempts to create the user using the UserService.
            userService.createUser(user);
            success = true;
            message = "Registration successful. Please check your email for verification.";
        } catch (IllegalArgumentException e) {
            // Catches any IllegalArgumentExceptions thrown during user creation, typically for validation errors.
            message = e.getMessage();  // The exception message is displayed to the user.
            model.addAttribute("errorMsg", e.getMessage());
        }

        // Adds feedback (message, action status, and success/failure flag) to the model for the response page.
        model.addAttribute("message", message);
        model.addAttribute("action", "register");
        model.addAttribute("success", success);

        // Returns the response view, which will inform the user of the outcome of the registration process.
        return "/password/response";
    }

    @GetMapping("/confirm-account")  // Maps HTTP GET requests to "/confirm-account" for account confirmation.
    public String confirmAccount(@RequestParam("token") String token, Model model) {
        // Confirms the user's account based on the provided token (usually sent via email).
        boolean confirmed = userService.confirmUserAccount(token);

        if (confirmed) {
            // If the token is valid and the account is successfully confirmed, adds a success message to the model.
            model.addAttribute("message", "Your account has been successfully confirmed.");
            return "/password/response";  // Returns a response page informing the user of successful account confirmation.
        } else {
            // If the token is invalid or expired, adds an error message to the model.
            model.addAttribute("message", "The confirmation link is invalid or expired.");
            return "/password/response";  // Returns the same response page with an error message.
        }
    }
}