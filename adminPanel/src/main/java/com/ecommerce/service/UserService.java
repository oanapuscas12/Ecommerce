package com.ecommerce.service;

import com.ecommerce.model.Merchant;
import com.ecommerce.model.User;
import com.ecommerce.repository.MerchantRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailSenderService emailSenderService;

    @Value("${domain}")
    private String domain;

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

        String token = generateToken();
        user.setToken(token);
        User savedUser = userRepository.save(user);

        String confirmationLink = domain + "/confirm-account?token=" + token;
        String subject = "Confirm Your Account";
        String body = "Hello " + user.getUsername() + ",\n\nPlease confirm your account by clicking the link below:\n" + confirmationLink + "\n\nBest regards,\nThe Team";
//        emailSenderService.sendEmail(user.getEmail(), subject, body);

        return savedUser;
    }

    public boolean confirmUserAccount(String token) {
        Optional<User> userOptional = userRepository.findByToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(true);
            user.setToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void deactivateUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setActive(false);
            userRepository.save(existingUser);
        }
    }

    public void activateUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
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

    public Page<User> getAllAdmins(Pageable pageable) {
        return userRepository.findByIsAdmin(true, pageable);
    }

    public Page<User> getAllMerchants(Pageable pageable) {
        return userRepository.findByIsAdmin(false, pageable);
    }

    public void updateUser(Long id, User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setAdmin(updatedUser.isAdmin());
            existingUser.setActive(updatedUser.isActive());
            userRepository.save(existingUser);
        }
    }

    public boolean initiatePasswordRecover(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = generateToken();
            user.setToken(token);
            userRepository.save(user);

            String resetLink = domain + "/reset-password?token=" + token;
            String body = "To reset your password, click the link below:\n" + resetLink;

//            emailSenderService.sendEmail(email, "Password Reset Request", body);
            return true;
        }
        return false;
    }

    public boolean validatePasswordResetToken(String token) {
        return userRepository.findByToken(token).isPresent();
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        } else {
            return null;
        }
    }

    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.isAdmin();
    }

    public boolean isMerchant() {
        User user = getCurrentUser();
        return user != null && !user.isAdmin();
    }

    public List<User> getNonAdminUsersExcluding(Long excludeUserId) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.isAdmin() && !user.getId().equals(excludeUserId))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getAllMonthlyMerchantEnrollments(int year) {
        List<User> allUsers = userRepository.findAll();

        List<User> merchantUsers = allUsers.stream()
                .filter(user -> !user.isAdmin())
                .collect(Collectors.toList());

        Map<String, Long> monthlyEnrollments = new HashMap<>();

        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(year, month);

            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();

            LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
            LocalDateTime endOfMonthDateTime = endOfMonth.atTime(LocalTime.MAX);

            long count = merchantUsers.stream()
                    .filter(user -> {
                        LocalDateTime userCreatedDateTime = user.getCreatedDate();
                        return (userCreatedDateTime.isEqual(startOfMonthDateTime) || userCreatedDateTime.isAfter(startOfMonthDateTime)) &&
                                (userCreatedDateTime.isEqual(endOfMonthDateTime) || userCreatedDateTime.isBefore(endOfMonthDateTime));
                    })
                    .count();
            monthlyEnrollments.put(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), count);
        }

        return monthlyEnrollments;
    }

    public Map<String, Long> getMerchantCountByCounty() {
        List<Merchant> allMerchants = merchantRepository.findAll();

        return allMerchants.stream()
                .collect(Collectors.groupingBy(Merchant::getCounty, Collectors.counting()));
    }

    public Map<String, Long> getMerchantActivityForCurrentMonth() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<Merchant> merchants = merchantRepository.findAll();

        long newMerchantsCount = merchants.stream()
                .filter(merchant -> {
                    LocalDate createdDate = merchant.getCreatedDate().toLocalDate();
                    return !createdDate.isBefore(startOfMonth) && !createdDate.isAfter(endOfMonth);
                })
                .count();

        long returningMerchantsCount = merchants.stream()
                .filter(merchant -> {
                    LocalDateTime lastLoginDate = merchant.getLastLoginDate();
                    LocalDate createdDate = merchant.getCreatedDate().toLocalDate();
                    return lastLoginDate != null &&
                            !createdDate.isAfter(startOfMonth) &&
                            lastLoginDate.toLocalDate().isAfter(startOfMonth.minusDays(1)) &&
                            lastLoginDate.toLocalDate().isBefore(endOfMonth.plusDays(1));
                })
                .count();

        long inactiveMerchantsCount = merchants.stream()
                .filter(merchant -> {
                    LocalDateTime lastLoginDate = merchant.getLastLoginDate();
                    return lastLoginDate == null ||
                            lastLoginDate.toLocalDate().isBefore(startOfMonth) ||
                            lastLoginDate.toLocalDate().isAfter(endOfMonth);
                })
                .count();

        Map<String, Long> activityCounts = new HashMap<>();
        activityCounts.put("New", newMerchantsCount);
        activityCounts.put("Returning", returningMerchantsCount);
        activityCounts.put("Inactive", inactiveMerchantsCount);

        return activityCounts;
    }
}
