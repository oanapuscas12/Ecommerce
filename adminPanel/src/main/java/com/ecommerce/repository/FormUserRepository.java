package com.ecommerce.repository;

import com.ecommerce.model.FormUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormUserRepository extends JpaRepository<FormUser, Long> {
}
