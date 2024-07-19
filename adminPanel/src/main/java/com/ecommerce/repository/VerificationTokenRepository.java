package com.ecommerce.repository;

import com.ecommerce.model.User;
import com.ecommerce.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findOneByToken(String token);
    Optional<VerificationToken> findOneByUser(User user);
}
