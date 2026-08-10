package com.mdsrtech.backend.domain.dtos.customresponses.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResponseDTO {

    private String message;
    private Long orderId;

}