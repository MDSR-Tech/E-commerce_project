package com.mdsrtech.backend.domain.dtos.customresponses.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyPromoResponseDTO {

    private String message;
    private String promoCode;
    private Integer discountPercent;

}