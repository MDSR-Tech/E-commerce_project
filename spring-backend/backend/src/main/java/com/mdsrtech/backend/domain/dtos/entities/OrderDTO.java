package com.mdsrtech.backend.domain.dtos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private UUID userId;
    private Integer subtotalCents;
    private Integer taxCents;
    private Integer shippingCents;
    private Integer totalCents;
    private String currency;
    private Long paymentId;

}