package com.spendwise.repository;

import com.spendwise.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByIsSystemTrue();

    List<Category> findByIsSystemTrueOrUserId(UUID userId);

    Optional<Category> findByNameAndIsSystemTrue(String name);

    List<Category> findByUserId(UUID userId);

    Optional<Category> findByNameAndUserId(String name, UUID userId);

    boolean existsByNameAndUserIsNull(String name);

    boolean existsByNameAndUserId(String name, UUID userId);

    @Query("""
    SELECT c
    FROM Category c
    WHERE LOWER(c.name) = LOWER(:name)
    AND (c.isSystem = true OR c.user.id = :userId)
    """)
    Optional<Category> findAvailableCategory(
            @Param("name") String name,
            @Param("userId") UUID userId
    );
}
