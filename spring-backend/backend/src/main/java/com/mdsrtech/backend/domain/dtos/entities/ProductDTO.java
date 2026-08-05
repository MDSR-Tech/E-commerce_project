package com.mdsrtech.backend.domain.dtos.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
        private Long id;
        private String title;
        private String slug;
        private BrandDTO brand;
        private CategoryDTO category;
        private String description;
        @JsonProperty("price_cents")
        private Integer priceCents;
        private String currency;
        private Integer stock;
        @JsonProperty("is_on_sale")
        private Boolean isOnSale;
        @JsonProperty("sale_percent")
        private Integer salePercent;
        @JsonProperty("sale_price_cents")
        private Integer salePriceCents;
        private ProductImageDTO image;
}
