package com.mdsrtech.backend.domain.dtos.customresponses.wishlists;

import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishListItemDTO {

    private Long id;
    private Long productId;
    private ProductDTO product;


}