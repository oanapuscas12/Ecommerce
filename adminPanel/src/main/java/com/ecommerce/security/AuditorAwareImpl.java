package com.ecommerce.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component  // Indicates that this class is a Spring-managed component.
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Retrieves the current authentication object from the security context.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If there is no authentication or the user is not authenticated, return an empty Optional.
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Retrieves the principal (user) from the authentication object.
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // If the principal is of type UserDetails, get the username from it.
            return Optional.of(((UserDetails) principal).getUsername());
        } else if (principal instanceof String) {
            // If the principal is of type String, directly use it as the username.
            return Optional.of((String) principal);
        } else {
            // If the principal is neither of these types, return an empty Optional.
            return Optional.empty();
        }
    }
}