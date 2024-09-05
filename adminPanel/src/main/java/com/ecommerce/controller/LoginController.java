package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller  // Marks the class as a Spring MVC controller, handling HTTP requests.
public class LoginController {

    @Autowired
    private UserService userService;  // Injects the UserService for user-related operations.

    @GetMapping("/")  // Maps the root URL ("/") to the `startPage` method.
    public String startPage() {
        // Returns the login view when the root URL is accessed.
        return "login";
    }

    @GetMapping("/login")  // Maps the "/login" URL to the `login` method.
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        // Handles login-related actions, including displaying error or logout messages.

        if (error != null) {
            // If the "error" parameter is present, add an error message to the model.
            model.addAttribute("errorMsg", "Invalid username or password.");
        }
        if (logout != null) {
            // If the "logout" parameter is present, add a logout message to the model.
            model.addAttribute("logoutMsg", "You have been logged out successfully.");
        }
        // Returns the login view with the appropriate messages.
        return "login";
    }

    @GetMapping("/home")  // Maps the "/home" URL to the `general` method.
    public String general(Model model) {
        // Handles the home page view for users after login.

        User user = userService.getCurrentUser();  // Retrieves the current logged-in user from the UserService.
        boolean isAdmin = user.isAdmin();  // Checks if the current user is an admin.

        // Adds user information and role-based attributes to the model for rendering in the view.
        model.addAttribute("user", user);
        model.addAttribute("admin", isAdmin);  // Sets "admin" to true if the user is an admin.
        model.addAttribute("merchant", !isAdmin);  // Sets "merchant" to true if the user is not an admin.

        // Returns the "home" view, which will be rendered based on the user type (admin or merchant).
        return "home";
    }
}