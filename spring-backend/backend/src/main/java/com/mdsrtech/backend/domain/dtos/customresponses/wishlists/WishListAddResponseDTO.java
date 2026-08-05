package com.mdsrtech.backend.domain.dtos.customresponses.wishlists;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishListAddResponseDTO {

    private String message;
    private Long productId;
    private Long itemId;


}