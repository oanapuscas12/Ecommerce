package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.VerificationTokenRepository;
import com.ecommerce.model.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing user activation using verification tokens.
 */
@Service
public class VerificationTokenService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    /**
     * Activate a user based on the provided verification token.
     *
     * @param token The verification token used for user activation.
     * @return An Optional containing the activated user if the token is valid, or an empty Optional if the token is not found or invalid.
     */
    public Optional<User> activateUserForToken(final String token) {
        Optional<VerificationToken> optionalVerificationToken = verificationTokenRepository.findOneByToken(token);
        if (optionalVerificationToken.isPresent()) {
            VerificationToken verificationToken = optionalVerificationToken.get();
            User user = verificationToken.getUser();
            user.setActive(false);
            verificationToken.setTokenStatus(VerificationToken.TokenStatus.ACTIVATED_BY_USER);
            userRepository.save(user);
            verificationTokenRepository.save(verificationToken);
            return Optional.of(user);
        }

        return Optional.empty();
    }
}