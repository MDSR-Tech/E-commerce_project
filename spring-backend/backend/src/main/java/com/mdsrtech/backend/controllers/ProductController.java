package com.mdsrtech.backend.controllers;

import com.mdsrtech.backend.domain.dtos.customresponses.GetProductsByCategoriesResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.SearchResponseDTO;
import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import com.mdsrtech.backend.domain.entities.Product;
import com.mdsrtech.backend.mapper.Mapper;
import com.mdsrtech.backend.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api")
public class ProductController {

    private final ProductService productService;
    private final Mapper<Product, ProductDTO> productMapper;

    public ProductController(ProductService productService, Mapper<Product, ProductDTO> productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping(path = "/products")
    public List<ProductDTO> getProducts() {
        List<Product> products = productService.getAllProducts();
        return products.stream().map(productMapper::mapFromEntityToDTO).toList();
    }

    @GetMapping(path = "/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {

        Optional<Product> foundProduct = productService.getProductById(id);
        return foundProduct.map(product -> {
            ProductDTO productDTO = productMapper.mapFromEntityToDTO(product);
            return ResponseEntity.ok(productDTO);   // Same as new ResponseEntity<>(productDTO, HttpStatus.OK)
        }).orElse(ResponseEntity.notFound().build());   // Same as new ResponseEntity<>(HttpStatus.NOT_FOUND)

    }

    @GetMapping(path = "/products/slug/{slug}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String slug) {

        Optional<Product> foundProduct = productService.getProductBySlug(slug);
        return foundProduct.map(product -> {
            ProductDTO productDTO = productMapper.mapFromEntityToDTO(product);
            return ResponseEntity.ok(productDTO);   // Same as new ResponseEntity<>(productDTO, HttpStatus.OK)
        }).orElse(ResponseEntity.notFound().build());   // Same as new ResponseEntity<>(HttpStatus.NOT_FOUND)

    }

    @GetMapping(path = "/categories/{category_slug}/products")
    public ResponseEntity<GetProductsByCategoriesResponseDTO> getProductsByCategory(@PathVariable String category_slug) {

        GetProductsByCategoriesResponseDTO getProductsByCategoriesResponse = productService.getProductsByCategory(category_slug);
        return ResponseEntity.ok(getProductsByCategoriesResponse);

    }

    @GetMapping(path = "/search")
    public ResponseEntity<SearchResponseDTO> getProductsBySearchCriteria(@RequestParam(name = "q") String query) {

        SearchResponseDTO searchResponse = productService.getProductsBySearch(query);
        return ResponseEntity.ok(searchResponse);

    }

}
