package com.ecommerce.repository;

import com.ecommerce.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormUserRepository extends JpaRepository<Merchant, Long> {
}
