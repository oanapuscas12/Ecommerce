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

@Service  // Indicates that this class is a Spring service component.
public class UserService {

    @Autowired
    private UserRepository userRepository;  // Repository for user data.

    @Autowired
    private MerchantRepository merchantRepository;  // Repository for merchant data.

    @Autowired
    private PasswordEncoder passwordEncoder;  // Password encoder for handling user passwords.

    @Autowired
    private EmailSenderService emailSenderService;  // Service for sending emails.

    @Value("${domain}")
    private String domain;  // Base domain for email links.

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Retrieves all users from the repository.
     * @return a list of all users.
     */
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     * @param id the ID of the user.
     * @return an Optional containing the user if found, otherwise empty.
     */
    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Creates a new user and saves it to the repository.
     * @param user the user to be created.
     * @return the created user.
     * @throws IllegalArgumentException if the username or email already exists.
     */
    public User createUser(User user) {
        logger.info("Creating user with username: {}", user.getUsername());

        // Check if the username or email already exists.
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // Encode the user's password.
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        String token = generateToken();
        user.setToken(token);

        User savedUser;

        // Create and save either a User or Merchant based on user type.
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

        // Prepare and send confirmation email (commented out).
        String confirmationLink = domain + "/confirm-account?token=" + token;
        String subject = "Confirm Your Account";
        String body = "Hello " + user.getUsername() + ",\n\nPlease confirm your account by clicking the link below:\n" + confirmationLink + "\n\nBest regards,\nThe Team";
        // Uncomment to send email!! --->
        // emailSenderService.sendEmail(user.getEmail(), subject, body);
        logger.info("Confirmation email prepared for user: {}", user.getUsername());

        return savedUser;
    }

    /**
     * Confirms a user's account using the provided token.
     * @param token the token used to confirm the account.
     * @return true if the account was successfully confirmed, false otherwise.
     */
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

    /**
     * Deactivates a user by setting their account to inactive.
     * @param id the ID of the user to be deactivated.
     */
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

    /**
     * Activates a user by setting their account to active.
     * @param id the ID of the user to be activated.
     */
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

    /**
     * Checks if a username already exists in the repository.
     * @param username the username to check.
     * @return true if the username exists, false otherwise.
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Checks if an email already exists in the repository.
     * @param email the email to check.
     * @return true if the email exists, false otherwise.
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Retrieves all users with admin privileges.
     * @param pageable pagination information.
     * @return a page of admin users.
     */
    public Page<User> getAllAdmins(Pageable pageable) {
        return userRepository.findByIsAdmin(true, pageable);
    }

    /**
     * Retrieves all merchants with pagination.
     * @param pageable pagination information.
     * @return a page of merchants.
     */
    public Page<Merchant> getAllMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable);
    }

    /**
     * Updates an existing user's information.
     * @param id the ID of the user to be updated.
     * @param updatedUser the updated user information.
     */
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

    /**
     * Initiates a password recovery process for a user based on their email.
     * @param email the email of the user requesting password recovery.
     * @return true if the email exists and the recovery process was initiated, false otherwise.
     */
    public boolean initiatePasswordRecover(String email) {
        logger.info("Initiating password recovery for email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = generateToken();
            user.setToken(token);
            userRepository.save(user);

            // Prepare the password reset email.
            String resetLink = domain + "/reset-password?token=" + token;
            String body = "To reset your password, click the link below:\n" + resetLink;

            // emailSenderService.sendEmail(email, "Password Reset Request", body);
            logger.info("Password recovery email prepared for user: {}", user.getUsername());

            return true;
        }
        logger.warn("Email not found for password recovery: {}", email);
        return false;
    }

    /**
     * Validates a password reset token.
     * @param token the token to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validatePasswordResetToken(String token) {
        return userRepository.findByToken(token).isPresent();
    }

    /**
     * Resets the user's password using the provided token and new password.
     * @param token the token used for resetting the password.
     * @param newPassword the new password.
     * @return true if the password was successfully reset, false otherwise.
     */
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

    /**
     * Generates a unique token for user verification or password reset.
     * @return a randomly generated token.
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieves the currently authenticated user.
     * @return the current user if authenticated, null otherwise.
     */
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            logger.info("Fetching current user: {}", username);
            return userRepository.findByUsername(username).orElse(null);
        } else {
            logger.warn("No authenticated user found in the security context");
            return null;
        }
    }

    /**
     * Checks if the currently authenticated user has admin privileges.
     * @return true if the user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.isAdmin();
    }

    /**
     * Checks if the currently authenticated user is a merchant.
     * @return true if the user is a merchant, false otherwise.
     */
    public boolean isMerchant() {
        User user = getCurrentUser();
        return user != null && !user.isAdmin();
    }

    /**
     * Retrieves a list of non-admin users, excluding a specified user ID.
     * @param excludeUserId the ID of the user to exclude from the list.
     * @return a list of non-admin users excluding the specified user ID.
     */
    public List<User> getNonAdminUsersExcluding(Long excludeUserId) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.isAdmin() && !user.getId().equals(excludeUserId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the number of merchants enrolled each month for a given year.
     * @param year the year for which to retrieve monthly enrollments.
     * @return a map of month names to the number of new merchant enrollments.
     */
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

    /**
     * Retrieves the count of merchants by county.
     * @return a map of county names to the number of merchants in each county.
     */
    public Map<String, Long> getMerchantCountByCounty() {
        List<Merchant> allMerchants = merchantRepository.findAll();

        return allMerchants.stream()
                .filter(merchant -> merchant.getCounty() != null)
                .collect(Collectors.groupingBy(Merchant::getCounty, Collectors.counting()));
    }

    /**
     * Retrieves merchant activity statistics for the current month.
     * @return a map of activity types (New, Returning, Inactive) to counts.
     */
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

    /**
     * Retrieves a merchant by their ID.
     * @param id the ID of the merchant.
     * @return an Optional containing the merchant if found, otherwise empty.
     */
    public Optional<Merchant> getMerchantById(Long id) {
        return merchantRepository.findById(id);
    }
}