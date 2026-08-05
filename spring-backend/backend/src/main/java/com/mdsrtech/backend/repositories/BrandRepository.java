package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    @Query("SELECT DISTINCT p.brand from Product p where p.category.id = :categoryId and p.isActive = true")
    List<Brand> getUniqueBrandsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("select distinct p.brand from Product p " +
            "where p.isActive = true and (" +
            "lower(p.title) like lower(concat('%', :query, '%')) or " +
            "lower(p.brand.name) like lower(concat('%', :query, '%')) or " +
            "lower(p.category.name) like lower(concat('%', :query, '%')))")
    List<Brand> findBySearch(@Param("query") String query);

}