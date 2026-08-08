package com.mdsrtech.backend.domain.dtos.customresponses.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartResponseDTO {

    private String message;
    private Long productId;
    private Integer quantity;
    private Long itemId;
    private Action action;

}