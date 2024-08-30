package com.ecommerce.controller;

import com.ecommerce.model.Merchant;
import com.ecommerce.model.User;
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

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/users")
    public String getUsers(@RequestParam(required = false) String role,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        logger.info("Fetching users list - page: {}, size: {}, role: {}", page, size, role);

        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.isAdmin();
        logger.debug("Current user: {}, isAdmin: {}", currentUser.getUsername(), isAdmin);

        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        try {
            if ("admin".equalsIgnoreCase(role)) {
                Page<User> userPage = userService.getAllAdmins(pageable);
                model.addAttribute("userPage", userPage);
                logger.info("Fetched admin users, page count: {}", userPage.getTotalPages());
            } else {
                Page<Merchant> userPage = userService.getAllMerchants(pageable);
                model.addAttribute("userPage", userPage);
                model.addAttribute("merchant", "merchant");
                logger.info("Fetched merchant users, page count: {}", userPage.getTotalPages());
            }

            String listTitle = "admin".equalsIgnoreCase(role) ? "Admins List" : "Merchants List";
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("role", role);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("otherRole", otherRole);
            model.addAttribute("pageTitle", listTitle);
        } catch (Exception e) {
            logger.error("Error fetching users list", e);
            throw e;
        }

        return "user/users";
    }

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable Long id, @RequestParam(required = false) String role, Model model) {
        logger.info("Fetching user details for user ID: {}", id);

        Optional<User> optionalUser = userService.getUserById(id);
        Optional<Merchant> optionalMerchant = userService.getMerchantById(id);
        User currentUser = userService.getCurrentUser();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            logger.debug("User found: {}", user.getUsername());

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

            return "user/user-details";
        } else {
            logger.warn("User not found with ID: {}", id);
            return "redirect:/user/users" + (role != null ? "?role=" + role : "");
        }
    }

    @GetMapping("/users/create")
    public String createUserForm(@RequestParam(value = "username", required = false) String username,
                                 @RequestParam(value = "email", required = false) String email,
                                 Model model) {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.isAdmin();
        String role = isAdmin ? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";

        model.addAttribute("user", new User());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "Create new user");
        model.addAttribute("role", role);

        return "user/create-user";
    }

    @GetMapping("/users/validateUsername")
    @ResponseBody
    public boolean validateUsername(@RequestParam("username") String username) {
        return userService.existsByUsername(username);
    }

    @GetMapping("/users/validateEmail")
    @ResponseBody
    public boolean validateEmail(@RequestParam("email") String email) {
        return userService.existsByEmail(email);
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user) {
        logger.info("Creating new user with username: {}", user.getUsername());
        userService.createUser(user);
        String role = user.isAdmin() ? "admin" : "merchant";
        return "redirect:/user/users?role=" + role;
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, @RequestParam(required = false) String role, Model model) {
        Optional<User> user = userService.getUserById(id);
        Optional<Merchant> merchant = userService.getMerchantById(id);

        if (user.isPresent()) {
            User currentUser = userService.getCurrentUser();
            model.addAttribute("user", user.get());
            model.addAttribute("isAdmin", userService.isAdmin());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role != null ? role : "admin");
            model.addAttribute("otherRole", user.get().isAdmin() ? "Admin" : "Merchant");
            model.addAttribute("pageTitle", "Edit User: " + user.get().getUsername());
            merchant.ifPresent(value -> model.addAttribute("merchant", value));

            return "user/edit-user";
        }
        return "redirect:/user/users?role=" + (role != null ? role : "admin");
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, @ModelAttribute User user) {
        userService.updateUser(id, user);
        String role = user.isAdmin() ? "admin" : "merchant";
        return "redirect:/user/users?role=" + role;
    }

    @PostMapping("/users/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id) {
        logger.info("Deactivating user with ID: {}", id);
        userService.deactivateUser(id);
        try {
            User user = userService.getUserById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
            String role = user.isAdmin() ? "admin" : "merchant";
            logger.debug("User deactivated: {}", user.getUsername());
            return "redirect:/user/users?role=" + role;
        } catch (NoSuchElementException e) {
            logger.error("User not found during deactivation: {}", id, e);
            return "redirect:/user/users";
        }
    }

    @PostMapping("/users/activate/{id}")
    public String activateUser(@PathVariable Long id) {
        logger.info("Activating user with ID: {}", id);
        userService.activateUser(id);
        try {
            User user = userService.getUserById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
            String role = user.isAdmin() ? "admin" : "merchant";
            logger.debug("User activated: {}", user.getUsername());
            return "redirect:/user/users?role=" + role;
        } catch (NoSuchElementException e) {
            logger.error("User not found during activation: {}", id, e);
            return "redirect:/user/users";
        }
    }
}
