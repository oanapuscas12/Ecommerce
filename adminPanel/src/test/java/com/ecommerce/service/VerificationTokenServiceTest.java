package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.model.VerificationToken;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificationTokenServiceTest {

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActivateUserForToken() {
        User user = new User();
        user.setId(1L);
        user.setActive(false);

        VerificationToken token = new VerificationToken();
        token.setToken("valid-token");
        token.setUser(user);

        when(verificationTokenRepository.findOneByToken("valid-token")).thenReturn(Optional.of(token));

        Optional<User> activatedUser = verificationTokenService.activateUserForToken("valid-token");

        assertTrue(activatedUser.isPresent());
        assertTrue(activatedUser.get().isActive());
        assertEquals(VerificationToken.TokenStatus.ACTIVATED_BY_USER, token.getTokenStatus());

        verify(userRepository, times(1)).save(user);
        verify(verificationTokenRepository, times(1)).save(token);
    }
}