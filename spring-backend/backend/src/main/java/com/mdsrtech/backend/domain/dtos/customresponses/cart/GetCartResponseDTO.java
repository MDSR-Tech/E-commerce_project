package com.mdsrtech.backend.domain.dtos.customresponses.cart;

import com.mdsrtech.backend.domain.dtos.entities.CartItemDTO;
import com.mdsrtech.backend.domain.entities.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCartResponseDTO {

    private Long cartId;
    private List<CartItemDTO> items;
    private Integer itemCount;
    private Integer totalItems;
    private Integer subtotalCents;
    private Integer originalSubtotalCents;
    private Integer saleSavingCents;
    private Integer taxCents;
    private Double taxRate;
    private Integer shippingCents;
    private Integer totalCents;
    private String currency;

}