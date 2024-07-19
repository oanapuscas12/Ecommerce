package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void deactivateUser(Long id) {
        Optional<User> User = userRepository.findById(id);
        if (User.isPresent()) {
            User existingUser = User.get();
            existingUser.setActive(false);
            userRepository.save(existingUser);
        }
    }

    public void activateUser(Long id) {
        Optional<User> User = userRepository.findById(id);
        if (User.isPresent()) {
            User existingUser = User.get();
            existingUser.setActive(true);
            userRepository.save(existingUser);
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getAllMerchants() {
        return userRepository.findByIsAdmin(false);
    }

    public List<User> getAllAdmins() {
        return userRepository.findByIsAdmin(true);
    }
}
