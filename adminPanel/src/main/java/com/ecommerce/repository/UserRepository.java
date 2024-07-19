package com.ecommerce.repository;

import com.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrUsername(String email, String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailOrUsername(String email, String username);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = CURRENT_TIMESTAMP WHERE u.username = :username")
    void setLastLoginForUsername(@Param("username") String username);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = CURRENT_TIMESTAMP WHERE u.id = :id")
    void setLastLoginForId(@Param("id") Long id);

    List<User> findByIsAdmin(boolean isAdmin);
}
