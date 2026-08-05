package com.mdsrtech.backend.services;

import com.mdsrtech.backend.domain.dtos.customresponses.GetProductsByCategoriesResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.SearchResponseDTO;
import com.mdsrtech.backend.domain.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    // GET /api/products
    List<Product> getAllProducts();

    // GET /api/products/{id}
    Optional<Product> getProductById(Long id);

    // GET /api/products/slug/{slug}
    Optional<Product> getProductBySlug(String slug);

    // GET /api/categories/{categorySlug}/products
    GetProductsByCategoriesResponseDTO getProductsByCategory(String categorySlug);

    // GET /api/search?q=...
    SearchResponseDTO getProductsBySearch(String queryString);

}
