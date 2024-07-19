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

@Service
public class DatabaseAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authenticationToken) throws AuthenticationException {
        // No additional checks required
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken authenticationToken) throws AuthenticationException {
        boolean valid = true;

        final String password = (String) authenticationToken.getCredentials();
        if (password == null || password.isEmpty()) {
            this.logger.warn("Username {}: no password provided", userName);
            valid = false;
        }

        Optional<User> user = userRepository.findByUsername(userName);

        if (!user.isPresent()) {
            this.logger.warn("Username {}: user not found", userName);
            valid = false;
        } else {
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
        userRepository.setLastLoginForId(userEntity.getId());
        return new org.springframework.security.core.userdetails.User(userEntity.getUsername(), userEntity.getPassword(), userEntity.getAuthorities());
    }
}