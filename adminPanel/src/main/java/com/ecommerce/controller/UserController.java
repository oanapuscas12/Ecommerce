package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admins")
    public String getAllAdmins(Model model) {
        List<User> admins = userService.getAllAdmins();
        model.addAttribute("admins", admins);
        return "user/admins";
    }

    @GetMapping("/merchants")
    public String getAllMerchants(Model model) {
        List<User> merchants = userService.getAllMerchants();
        model.addAttribute("merchants", merchants);
        return "user/merchants";
    }

    @GetMapping("/admins/{id}")
    public String getUserById(@PathVariable Long id, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "user/user-details";
        }
        return "redirect:/user/admins";
    }

    @GetMapping("/merchants/{id}")
    public String getMerchantById(@PathVariable Long id, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "user/user-details";
        }
        return "redirect:/user/merchants";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "user/create-user";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user) {
        userService.createUser(user);
        return "redirect:/user/admins";
    }

    @PostMapping("/admins/deactivate/{id}")
    public String deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return "redirect:/user/admins";
    }

    @PostMapping("/merchants/deactivate/{id}")
    public String deactivateMerchant(@PathVariable Long id) {
        userService.deactivateUser(id);
        return "redirect:/user/merchants";
    }

    @PostMapping("/admins/activate/{id}")
    public String activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return "redirect:/user/admins";
    }

    @PostMapping("/merchants/activate/{id}")
    public String activateMerchant(@PathVariable Long id) {
        userService.activateUser(id);
        return "redirect:/user/merchants";
    }
}
