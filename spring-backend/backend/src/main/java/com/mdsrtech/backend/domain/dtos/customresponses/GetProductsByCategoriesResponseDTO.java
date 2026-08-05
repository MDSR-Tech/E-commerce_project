package com.mdsrtech.backend.domain.dtos.customresponses;

import com.mdsrtech.backend.domain.dtos.entities.BrandDTO;
import com.mdsrtech.backend.domain.dtos.entities.CategoryDTO;
import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetProductsByCategoriesResponseDTO {
    private List<ProductDTO> products;
    private CategoryDTO category;
    private List<BrandDTO> brands;
}
