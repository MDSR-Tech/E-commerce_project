package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.Brand;
import com.mdsrtech.backend.domain.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    @Query("select distinct p.category from Product p " +
            "where p.isActive = true and (" +
            "lower(p.title) like lower(concat('%', :query, '%')) or " +
            "lower(p.brand.name) like lower(concat('%', :query, '%')) or " +
            "lower(p.category.name) like lower(concat('%', :query, '%')))")
    List<Category> findBySearch(@Param("query") String query);

}