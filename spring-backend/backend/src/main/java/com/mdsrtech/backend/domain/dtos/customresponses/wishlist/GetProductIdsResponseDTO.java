package com.mdsrtech.backend.domain.dtos.customresponses.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProductIdsResponseDTO {

    private List<Long> productIds;


}