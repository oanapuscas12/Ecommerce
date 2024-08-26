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

import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String getUsers(@RequestParam(required = false) String role,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin= currentUser.isAdmin();
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        Page<User> userPage;
        if ("admin".equalsIgnoreCase(role)) {
            userPage = userService.getAllAdmins(pageable);
        } else {
            userPage = userService.getAllMerchants(pageable);
        }

        String listTitle = "admin".equalsIgnoreCase(role) ? "Admins List" : "Merchants List";
        if (role == null || role.isEmpty()) {
            listTitle = Objects.equals(role, "admin") ? "Admins List" : "Merchants List";
        }

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("role", role);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", listTitle);
        model.addAttribute("userPage", userPage);

        return "user/users";
    }

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable Long id, @RequestParam(required = false) String role, Model model) {
        Optional<User> optionalUser = userService.getUserById(id);
        Optional<Merchant> optionalMerchant = userService.getMerchantById(id);
        User currentUser = userService.getCurrentUser();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            model.addAttribute("isAdmin", userService.isAdmin());
            model.addAttribute("user", user);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role != null ? role : "admin");
            model.addAttribute("otherRole", user.isAdmin() ? "Admin" : "Merchant");
            model.addAttribute("pageTitle", "User Details: " + user.getUsername());

            if (optionalMerchant.isPresent()) {
                Merchant merchant = optionalMerchant.get();
                model.addAttribute("merchant", merchant);
            }

            return "user/user-details";
        }

        return "redirect:/user/users" + (role != null ? "?role=" + role : "");
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.isAdmin();
        String role = currentUser.isAdmin()? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(role) ? "merchant" : "admin";
        model.addAttribute("user", new User());
        model.addAttribute("isAdmin", userService.isAdmin());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "Create new user");
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", isAdmin);
        return "user/create-user";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user) {
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
