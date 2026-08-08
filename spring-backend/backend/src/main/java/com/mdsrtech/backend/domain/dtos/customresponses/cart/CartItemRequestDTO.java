package com.mdsrtech.backend.domain.dtos.customresponses.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequestDTO {

    private Long productId;
    private Integer quantity;

}