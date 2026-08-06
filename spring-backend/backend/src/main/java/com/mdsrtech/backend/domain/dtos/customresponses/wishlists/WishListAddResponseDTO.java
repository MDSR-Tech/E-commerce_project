package com.mdsrtech.backend.domain.dtos.customresponses.wishlists;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishListAddResponseDTO {

    private String message;
    private Long productId;
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long itemId;


}