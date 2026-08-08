package com.mdsrtech.backend.domain.dtos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Integer unitPriceCents;
    private Integer originalPriceCents;
    private Integer lineTotalCents;
    private Integer originalLineTotalCents;
    private Instant addedAt;
    private ProductDTO product;

}