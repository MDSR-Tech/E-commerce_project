package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.domain.dtos.customresponses.GetProductsByCategoriesResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.SearchResponseDTO;
import com.mdsrtech.backend.domain.dtos.entities.BrandDTO;
import com.mdsrtech.backend.domain.dtos.entities.CategoryDTO;
import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import com.mdsrtech.backend.domain.entities.Brand;
import com.mdsrtech.backend.domain.entities.Category;
import com.mdsrtech.backend.domain.entities.Product;
import com.mdsrtech.backend.mapper.Mapper;
import com.mdsrtech.backend.repositories.BrandRepository;
import com.mdsrtech.backend.repositories.CategoryRepository;
import com.mdsrtech.backend.repositories.ProductRepository;
import com.mdsrtech.backend.services.ProductService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final Mapper<Product, ProductDTO> productMapper;

    public ProductServiceImpl(ProductRepository productRepository, BrandRepository brandRepository, CategoryRepository categoryRepository, Mapper<Product, ProductDTO> productMapper) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public GetProductsByCategoriesResponseDTO getProductsByCategory(String categorySlug) {

        Category category = categoryRepository.findBySlug(categorySlug).orElseThrow(
                () -> new RuntimeException("Category with slug " + categorySlug + " does not exist")
        );

        List<Product> products = productRepository.findByCategoryId(category.getId());
        List<ProductDTO> productDTOS = products.stream().map(productMapper::mapFromEntityToDTO).toList();

        List<BrandDTO> brandDTOS = brandRepository.getUniqueBrandsByCategoryId(category.getId()).stream()
                .map(brand -> BrandDTO.builder()
                        .id(brand.getId())
                        .name(brand.getName())
                        .slug(brand.getSlug())
                        .build())
                .toList();

        return GetProductsByCategoriesResponseDTO.builder()
                .products(productDTOS)
                .brands(brandDTOS)
                .category(CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .salePercent(category.getSalePercent())
                        .build())
                .build();

    }

    @Override
    public SearchResponseDTO getProductsBySearch(String queryString) {

        List<Product> products = productRepository.findBySearch(queryString);
        List<ProductDTO> productDTOS = products.stream().map(productMapper::mapFromEntityToDTO).toList();

        List<Brand> brands = brandRepository.findBySearch(queryString);
        List<BrandDTO> brandDTOS = brands.stream().map(brand -> BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .build()).toList();

        List<Category> categories = categoryRepository.findBySearch(queryString);
        List<CategoryDTO> categoryDTOS = categories.stream().map(category -> CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .salePercent(category.getSalePercent())
                .build()).toList();

        return SearchResponseDTO.builder()
                .products(productDTOS)
                .brands(brandDTOS)
                .categories(categoryDTOS)
                .total(productDTOS.size())
                .build();

    }

}
