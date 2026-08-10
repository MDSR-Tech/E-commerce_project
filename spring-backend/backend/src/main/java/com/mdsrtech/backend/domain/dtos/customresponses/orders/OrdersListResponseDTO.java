package com.mdsrtech.backend.domain.dtos.customresponses.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersListResponseDTO {

    private Long id;
    private Integer totalCents;
    private Integer subtotalCents;
    private Integer taxCents;
    private Integer shippingCents;
    private String currency;
    @Nullable
    private Instant placedAt;
    private Integer itemCount;
    private String previewImage;
    @Nullable
    private String firstItemName;

}