package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String getUsers(@RequestParam(required = false) String role, Model model) {
        User currentUser = userService.getCurrentUser();
        List<User> users;
        String pageRole = role != null ? role : "admin";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";

        if ("admin".equalsIgnoreCase(pageRole)) {
            users = userService.getAllAdmins();
        } else if ("merchant".equalsIgnoreCase(pageRole)) {
            users = userService.getAllMerchants();
        } else {
            return "redirect:/user/users?role=admin";
        }

        users.sort(Comparator.comparing(User::getUsername));

        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", pageRole.substring(0, 1).toUpperCase() + pageRole.substring(1) + "s List");
        model.addAttribute("users", users);

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
    public String createUserForm(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", new User());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", "Admin");
        model.addAttribute("pageTitle", "Create new User");
        return "user/create-user";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, @RequestParam String role) {
        userService.createUser(user);
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
    public String editUser(@PathVariable Long id, @ModelAttribute User user, @RequestParam String role) {
        userService.updateUser(id, user);
        String newRole = user.isAdmin() ? "admin" : "merchant";
        return "redirect:/user/users?role=" + newRole;
    }

    @PostMapping("/users/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id, @RequestParam String role) {
        userService.deactivateUser(id);
        return "redirect:/user/users?role=" + role;
    }

    @PostMapping("/users/activate/{id}")
    public String activateUser(@PathVariable Long id, @RequestParam String role) {
        userService.activateUser(id);
        return "redirect:/user/users?role=" + role;
    }
}
