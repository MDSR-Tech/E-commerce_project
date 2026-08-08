package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndIsActiveTrue(Long id);
    Optional<Product> findBySlug(String slug);
    List<Product> findByCategoryId(Long category_id);

    @Query("select p from Product p " +
            "left join p.brand b " +
            "left join p.category c " +
            "where p.isActive = true and (" +
            "lower(p.title) like lower(concat('%', :query, '%')) or " +
            "lower(p.description) like lower(concat('%', :query, '%')) or " +
            "lower(b.name) like lower(concat('%', :query, '%')) or " +
            "lower(c.name) like lower(concat('%', :query, '%')))")
    List<Product> findBySearch(@Param("query") String slug);

}