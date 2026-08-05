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
public class SearchResponseDTO {

    List<ProductDTO> products;
    String query;
    List<BrandDTO> brands;
    List<CategoryDTO> categories;
    Integer total;

}
