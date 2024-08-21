package com.ecommerce.controller;

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

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        User currentUser = userService.getCurrentUser();
        String pageRole = currentUser.isAdmin() ? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        Page<User> userPage;
        if ("admin".equalsIgnoreCase(pageRole)) {
            userPage = userService.getAllAdmins(pageable);
        } else {
            userPage = userService.getAllMerchants(pageable);
        }

        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", pageRole.substring(0, 1).toUpperCase() + pageRole.substring(1) + "s List");
        model.addAttribute("userPage", userPage);

        return "user/users";
    }

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable Long id, @RequestParam(required = false) String role, Model model) {
        Optional<User> user = userService.getUserById(id);
        User currentUser = userService.getCurrentUser();
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role != null ? role : "admin");
            model.addAttribute("otherRole", user.get().isAdmin() ? "Admin" : "Merchant");
            model.addAttribute("pageTitle", "User Details: " + user.get().getUsername());
            return "user/user-details";
        }
        return "redirect:/user/users" + (role != null ? "?role=" + role : "");
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model, @RequestParam(required = false) String role) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", new User());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", "Admin");
        model.addAttribute("pageTitle", "Create new user");
        model.addAttribute("role", role != null ? role : "admin");
        return "user/create-user";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, Model model) {
        userService.createUser(user);
        String role = user.isAdmin() ? "admin" : "merchant";
        return "redirect:/user/users?role=" + role;
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, @RequestParam(required = false) String role, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            User currentUser = userService.getCurrentUser();
            model.addAttribute("user", user.get());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role != null ? role : "admin");
            model.addAttribute("otherRole", user.get().isAdmin() ? "Admin" : "Merchant");
            model.addAttribute("pageTitle", "Edit User: " + user.get().getUsername());
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
        userService.deactivateUser(id);
        try {
            User user = userService.getUserById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
            String role = user.isAdmin() ? "admin" : "merchant";
            return "redirect:/user/users?role=" + role;
        } catch (NoSuchElementException e) {
            return "redirect:/user/users";
        }
    }

    @PostMapping("/users/activate/{id}")
    public String activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        try {
            User user = userService.getUserById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
            String role = user.isAdmin() ? "admin" : "merchant";
            return "redirect:/user/users?role=" + role;
        } catch (NoSuchElementException e) {
            return "redirect:/user/users";
        }
    }
}
