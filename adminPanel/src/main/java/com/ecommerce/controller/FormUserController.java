package com.ecommerce.controller;

import com.ecommerce.model.Merchant;
import com.ecommerce.model.User;
import com.ecommerce.service.FormUserService;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class FormUserController {

    @Autowired
    private FormUserService formUserService;

    @Autowired
    private UserService userService;

    @GetMapping("/api/form-users")
    public String getFormUsersPage(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Merchant> userPage = formUserService.getAllFormUsers(pageable);
        User currentUser = userService.getCurrentUser();
        String pageRole = currentUser.isAdmin() ? "admin" : "merchant";
        String otherRole = "admin".equalsIgnoreCase(pageRole) ? "merchant" : "admin";
        model.addAttribute("isAdmin", userService.isAdmin());
        model.addAttribute("role", pageRole);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherRole", otherRole);
        model.addAttribute("pageTitle", "Form Users List");
        model.addAttribute("users", formUserService.getAllFormUsers(PageRequest.of(0, Integer.MAX_VALUE)));
        model.addAttribute("userPage", userPage);
        return "user/form-users";
    }
}