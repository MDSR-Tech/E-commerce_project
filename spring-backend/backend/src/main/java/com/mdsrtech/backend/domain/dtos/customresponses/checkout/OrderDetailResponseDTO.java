package com.mdsrtech.backend.domain.dtos.customresponses.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponseDTO {

    private Boolean success;
    private Long orderId;
    private Boolean alreadyProcessed;

}