package com.ecommerce.controller;

import com.ecommerce.model.Merchant;
import com.ecommerce.model.User;
import com.ecommerce.service.MerchantService;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Controller class responsible for handling user-related HTTP requests.
@Controller
@RequestMapping("/user")  // Base URL for all methods in this controller.
public class UserController {

    @Autowired
    private UserService userService;  // Injects UserService for performing user-related operations.

    @Autowired
    private MerchantService merchantService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);  // Logger for this class.

    @GetMapping("/users")  // Handles GET requests to "/user/users".
    public String getUsers(@RequestParam(required = false) String role,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        // Logs the request parameters for fetching user lists.
        logger.info("Fetching users list - page: {}, size: {}, role: {}", page, size, role);

        User currentUser = userService.getCurrentUser();  // Gets the currently logged-in user.
        boolean isAdmin = currentUser.isAdmin();  // Checks if the current user is an admin.
        logger.debug("Current user: {}, isAdmin: {}", currentUser.getUsername(), isAdmin);

        // Determines the opposite role to toggle between admin and merchant views.
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());  // Creates a pageable object for pagination.

        try {
            // Fetches users based on the role parameter (admin or merchant).
            if ("admin".equalsIgnoreCase(role)) {
                Page<User> userPage = userService.getAllAdmins(pageable);
                model.addAttribute("userPage", userPage);  // Adds the user page to the model.
                logger.info("Fetched admin users, page count: {}", userPage.getTotalPages());
            } else {
                Page<Merchant> userPage = userService.getAllMerchants(pageable);
                model.addAttribute("userPage", userPage);  // Adds the merchant page to the model.
                model.addAttribute("merchant", "merchant");  // Sets an attribute to indicate the merchant role.
                logger.info("Fetched merchant users, page count: {}", userPage.getTotalPages());
            }

            // Adds additional attributes to the model for the view.
            String listTitle = "admin".equalsIgnoreCase(role) ? "Admins List" : "Merchants List";
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("role", role);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("otherRole", otherRole);
            model.addAttribute("pageTitle", listTitle);
        } catch (Exception e) {
            logger.error("Error fetching users list", e);  // Logs any exceptions that occur.
            throw e;  // Rethrows the exception to handle it globally.
        }

        // Returns the view name to be rendered.
        return "user/users";
    }

    @GetMapping("/users/{id}")  // Handles GET requests to "/user/users/{id}" to fetch details of a specific user.
    public String getUserById(@PathVariable Long id, @RequestParam(required = false) String role, Model model) {
        logger.info("Fetching user details for user ID: {}", id);

        Optional<User> optionalUser = userService.getUserById(id);  // Fetches user by ID.
        Optional<Merchant> optionalMerchant = userService.getMerchantById(id);  // Fetches merchant by ID if applicable.
        User currentUser = userService.getCurrentUser();  // Gets the currently logged-in user.

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            logger.debug("User found: {}", user.getUsername());

            // Adds user and role details to the model.
            model.addAttribute("isAdmin", userService.isAdmin());
            model.addAttribute("user", user);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role != null ? role : "admin");
            model.addAttribute("otherRole", user.isAdmin() ? "Admin" : "Merchant");
            model.addAttribute("pageTitle", "User Details: " + user.getUsername());

            if (optionalMerchant.isPresent()) {
                Merchant merchant = optionalMerchant.get();
                model.addAttribute("merchant", merchant);
                logger.debug("Merchant details found for user ID: {}", id);
            }

            return "user/user-details";  // Returns the view for displaying user details.
        } else {
            logger.warn("User not found with ID: {}", id);
            // Redirects to the user list if the user is not found.
            return "redirect:/user/users" + (role != null ? "?role=" + role : "");
        }
    }

    @GetMapping("/users/create")  // Handles GET requests to "/user/users/create" for showing the user creation form.
    public String createUserForm(@RequestParam(value = "username", required = false) String username,
                                 @RequestParam(value = "email", required = false) String email,
                                 Model model) {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.isAdmin();
        String role = isAdmin ? "admin" : "merchant";  // Sets the role based on the current user's role.
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";

        // Prepares the model with attributes for creating a new user.
        model.addAttribute("user", new User());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "Create new user");
        model.addAttribute("role", role);

        return "user/create-user";  // Returns the view for the user creation form.
    }

    @GetMapping("/users/validateUsername")  // Handles GET requests to validate a username.
    @ResponseBody
    public boolean validateUsername(@RequestParam("username") String username) {
        return userService.existsByUsername(username);  // Checks if the username exists.
    }

    @GetMapping("/users/validateEmail")  // Handles GET requests to validate an email.
    @ResponseBody
    public boolean validateEmail(@RequestParam("email") String email) {
        return userService.existsByEmail(email);  // Checks if the email exists.
    }

    @PostMapping("/users")  // Handles POST requests to create a new user.
    public String createUser(@ModelAttribute User user) {
        logger.info("Creating new user with username: {}", user.getUsername());
        userService.createUser(user);  // Calls service to create the user.
        String role = user.isAdmin() ? "admin" : "merchant";
        return "redirect:/user/users?role=" + role;  // Redirects to the user list page.
    }

    @GetMapping("/users/edit/{id}")  // Handles GET requests to show the user edit form.
    public String editUserForm(@PathVariable Long id, @RequestParam(required = false) String role, Model model, @PathVariable(required = false) String changeToMerchant) {
        Optional<User> user = userService.getUserById(id);  // Fetches the user by ID.
        Optional<Merchant> merchant = userService.getMerchantById(id);  // Fetches the merchant by ID if applicable.

        if (user.isPresent()) {
            User currentUser = userService.getCurrentUser();
            model.addAttribute("user", user.get());
            model.addAttribute("isAdmin", userService.isAdmin());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role != null ? role : "admin");
            model.addAttribute("otherRole", user.get().isAdmin() ? "Admin" : "Merchant");
            model.addAttribute("pageTitle", "Edit User: " + user.get().getUsername());
            if (changeToMerchant == null) {
                merchant.ifPresent(value -> model.addAttribute("merchant", value));
            } else {
                if (merchant.isPresent()) {
                    model.addAttribute("merchant", merchant.get());
                } else {
                    Merchant merch = (Merchant) user.get();
                    merch.setMerchantMode(true);
                    model.addAttribute("merchant", merch);
                }
            }
            return "user/edit-user";
        }
        return "redirect:/user/users?role=" + (role != null ? role : "admin");  // Redirects to the user list if user is not found.
    }

    @PostMapping("/users/edit/{id}")  // Handles POST requests to update user details.
    public String editUser(@PathVariable Long id, @ModelAttribute User user) {
        String role = user.isAdmin() ? "admin" : "merchant";
        if (user.isAdmin()) {
            userService.updateUser(id, user);  // Calls service to update the user.
            return "redirect:/user/users?role=" + role;  // Redirects to the user list page.
        } else {
            // TODO find merchant and set it to merchantmode true if found + save
            //merchantService.deleteMerchant(id);
            return "redirect:/user/users/edit/" + user.getId() + "?changeToMerchant=true";
        }
    }

    @PostMapping("/users/edit/merchant/{id}")  // Handles POST requests to update user details.
    public String editMerchant(@PathVariable Long id, @ModelAttribute User user, @ModelAttribute Merchant merchant) {
        String role = user.isAdmin() ? "admin" : "merchant";
        if (merchant != null) {
            merchant.setMerchantMode(!user.isAdmin());
//            merchantService.updateMerchant(id, merchant);
        }
        if (user.isAdmin()) {
            userService.updateUser(id, user);
        } else {
            userService.updateMerchant(id, user, merchant);
        }
        return "redirect:/user/users?role=" + role;
    }

    @PostMapping("/users/deactivate/{id}")  // Handles POST requests to deactivate a user.
    public String deactivateUser(@PathVariable Long id) {
        logger.info("Deactivating user with ID: {}", id);
        userService.deactivateUser(id);  // Calls service to deactivate the user.
        try {
            User user = userService.getUserById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
            String role = user.isAdmin() ? "admin" : "merchant";
            logger.debug("User deactivated: {}", user.getUsername());
            return "redirect:/user/users?role=" + role;  // Redirects to the user list page.
        } catch (NoSuchElementException e) {
            logger.error("User not found during deactivation: {}", id, e);
            return "redirect:/user/users";  // Redirects to the user list page if user is not found.
        }
    }

    @PostMapping("/users/activate/{id}")  // Handles POST requests to activate a user.
    public String activateUser(@PathVariable Long id) {
        logger.info("Activating user with ID: {}", id);
        userService.activateUser(id);  // Calls service to activate the user.
        try {
            User user = userService.getUserById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
            String role = user.isAdmin() ? "admin" : "merchant";
            logger.debug("User activated: {}", user.getUsername());
            return "redirect:/user/users?role=" + role;  // Redirects to the user list page.
        } catch (NoSuchElementException e) {
            logger.error("User not found during activation: {}", id, e);
            return "redirect:/user/users";  // Redirects to the user list page if user is not found.
        }
    }
}