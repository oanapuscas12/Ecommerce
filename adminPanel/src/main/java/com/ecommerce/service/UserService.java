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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
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

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        logger.info("Creating user with username: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        String token = generateToken();
        user.setToken(token);

        User savedUser;
        if (user.isAdmin()) {
            savedUser = userRepository.save(user);
            logger.info("Admin user created with ID: {}", savedUser.getId());
        } else {
            Merchant merchant = new Merchant();
            merchant.setUsername(user.getUsername());
            merchant.setPassword(encodedPassword);
            merchant.setEmail(user.getEmail());
            merchant.setToken(token);
            merchant.setName(user.getUsername());
            merchant.setStoreLaunched(false);
            merchant.setStoreActive(false);
            savedUser = merchantRepository.save(merchant);
            logger.info("Merchant created with ID: {}", savedUser.getId());
        }

        String confirmationLink = domain + "/confirm-account?token=" + token;
        String subject = "Confirm Your Account";
        String body = "Hello " + user.getUsername() + ",\n\nPlease confirm your account by clicking the link below:\n" + confirmationLink + "\n\nBest regards,\nThe Team";
        // Uncomment to send email!! --->
        // emailSenderService.sendEmail(user.getEmail(), subject, body);
        logger.info("Confirmation email prepared for user: {}", user.getUsername());

        return savedUser;
    }

    public boolean confirmUserAccount(String token) {
        Optional<User> userOptional = userRepository.findByToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(true);
            user.setToken(null);
            userRepository.save(user);
            logger.info("User account confirmed for username: {}", user.getUsername());
            return true;
        }
        logger.warn("Invalid token provided for account confirmation: {}", token);
        return false;
    }

    public void deactivateUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setActive(false);
            userRepository.save(existingUser);
            logger.info("User deactivated: {}", existingUser.getUsername());
        } else {
            logger.warn("User not found for deactivation with ID: {}", id);
        }
    }

    public void activateUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setActive(true);
            userRepository.save(existingUser);
            logger.info("User activated: {}", existingUser.getUsername());
        } else {
            logger.warn("User not found for activation with ID: {}", id);
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

    public Page<Merchant> getAllMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable);
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
        logger.info("Initiating password recovery for email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = generateToken();
            user.setToken(token);
            userRepository.save(user);

            String resetLink = domain + "/reset-password?token=" + token;
            String body = "To reset your password, click the link below:\n" + resetLink;

            // emailSenderService.sendEmail(email, "Password Reset Request", body);
            logger.info("Password recovery email prepared for user: {}", user.getUsername());

            return true;
        }
        logger.warn("Email not found for password recovery: {}", email);
        return false;
    }

    public boolean validatePasswordResetToken(String token) {
        return userRepository.findByToken(token).isPresent();
    }

    public boolean resetPassword(String token, String newPassword) {
        logger.info("Resetting password for token: {}", token);
        Optional<User> userOptional = userRepository.findByToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setToken(null);
            userRepository.save(user);
            logger.info("Password reset successfully for user: {}", user.getUsername());
            return true;
        }
        logger.warn("Invalid token provided for password reset: {}", token);
        return false;
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            logger.info("Fetching current user: {}", username);

            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("User not found with username: " + username));
        } else {
            logger.warn("No authenticated user found in the security context");
            throw new IllegalStateException("No authenticated user found in the security context");
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
                .filter(merchant -> merchant.getCounty() != null)
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

    public Optional<Merchant> getMerchantById(Long id) {
        return merchantRepository.findById(id);
    }
}
