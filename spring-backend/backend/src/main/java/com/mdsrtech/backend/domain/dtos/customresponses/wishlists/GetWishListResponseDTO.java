package com.mdsrtech.backend.domain.dtos.customresponses.wishlists;

import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetWishListResponseDTO {

    private Long wishlistId;
    private List<WishListItemDTO> items;
    private Integer count;



}