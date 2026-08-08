package com.mdsrtech.backend.domain.dtos.customresponses.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveItemFromWishListDTO {

    private String message;
    private Long productId;


}