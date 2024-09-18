package com.ecommerce.repository;

import com.ecommerce.model.Document;
import com.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @NonNull
    Page<Document> findByUploadedById(@NonNull Long userId, @NonNull Pageable pageable);

    @NonNull
    Page<Document> findAll(@NonNull Pageable pageable);

    Optional<Document> findByNameAndUploadedById(String name, Long userId);

    @Query("SELECT DISTINCT d.uploadedBy FROM Document d")
    List<User> findDistinctMerchantsWithDocuments();
}
