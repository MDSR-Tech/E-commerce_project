package com.mdsrtech.backend.domain.dtos.customresponses.orders;

import com.mdsrtech.backend.domain.dtos.entities.OrderItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderDetailsResponseDTO {

    private Long id;
    private Integer subtotalCents;
    private Integer taxCents;
    private Integer shippingCents;
    private Integer totalCents;
    private String currency;
    private Instant placedAt;
    private List<OrderItemDTO> items;

}