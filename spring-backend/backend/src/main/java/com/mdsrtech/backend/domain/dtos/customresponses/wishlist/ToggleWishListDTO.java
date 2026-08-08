package com.mdsrtech.backend.domain.dtos.customresponses.wishlist;

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
public class ToggleWishListDTO {

    private String message;
    private Long productId;
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long itemId;
    private Action action;
    private boolean inWishlist;


}