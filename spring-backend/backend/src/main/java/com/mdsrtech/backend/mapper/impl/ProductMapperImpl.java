package com.mdsrtech.backend.mapper.impl;

import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import com.mdsrtech.backend.domain.entities.Product;
import com.mdsrtech.backend.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductMapperImpl implements Mapper<Product, ProductDTO> {

    private final ModelMapper modelMapper;

    public ProductMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductDTO mapFromEntityToDTO(Product product) {
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public Product mapFromDTOToEntity(ProductDTO productDTO) {
        return modelMapper.map(productDTO, Product.class);
    }

}
