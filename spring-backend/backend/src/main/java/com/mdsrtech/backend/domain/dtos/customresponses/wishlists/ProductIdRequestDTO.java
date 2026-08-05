package com.mdsrtech.backend.domain.dtos.customresponses.wishlists;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIdRequestDTO {

    private Long productId;


}