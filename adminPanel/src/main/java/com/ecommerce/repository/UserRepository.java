package com.ecommerce.repository;

import com.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = CURRENT_TIMESTAMP WHERE u.id = :id")
    void setLastLoginForId(@Param("id") Long id);

    Page<User> findByIsAdmin(boolean isAdmin, Pageable pageable);
    Optional<User> findByIsAdmin(boolean isAdmin);

    Optional<User> findByToken(String token);
}
