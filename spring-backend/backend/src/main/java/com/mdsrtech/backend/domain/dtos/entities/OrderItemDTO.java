package com.mdsrtech.backend.domain.dtos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private String title;
    private Integer quantity;
    private Integer unitPriceCents;
    private Integer lineTotalCents;
    private String imageUrl;
    private String productSlug;

}