package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service  // This class is a Spring service component.
public class DatabaseAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;  // Repository for accessing user data in the database.

    @Autowired
    private PasswordEncoder passwordEncoder;  // Encoder for comparing passwords.

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authenticationToken) throws AuthenticationException {
        // No additional checks required, password and username are handled in `retrieveUser` method.
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken authenticationToken) throws AuthenticationException {
        boolean valid = true;

        final String password = (String) authenticationToken.getCredentials();
        if (password == null || password.isEmpty()) {
            this.logger.warn("Username {}: no password provided", userName);
            valid = false;
        }

        // Fetch the user from the database.
        Optional<User> user = userRepository.findByUsername(userName);

        if (!user.isPresent()) {
            this.logger.warn("Username {}: user not found", userName);
            valid = false;
        } else {
            // Check if the provided password matches the stored password.
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                if (!user.get().isActive()) {
                    this.logger.warn("Username {}: not active", userName);
                    throw new AccountExpiredException("Account is not active anymore for user " + userName);
                }
            } else {
                this.logger.warn("Username {}: bad password entered", userName);
                valid = false;
            }
        }

        if (!valid) {
            throw new BadCredentialsException("Invalid Username/Password for user " + userName);
        }

        User userEntity = user.get();
        // Update the last login timestamp for the user.
        userRepository.setLastLoginForId(userEntity.getId());

        // Return a Spring Security User object with the user's username, password, and authorities.
        return new org.springframework.security.core.userdetails.User(userEntity.getUsername(), userEntity.getPassword(), userEntity.getAuthorities());
    }
}