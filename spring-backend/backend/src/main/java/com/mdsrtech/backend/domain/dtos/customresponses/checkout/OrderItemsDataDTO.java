package com.mdsrtech.backend.domain.dtos.customresponses.checkout;

import com.mdsrtech.backend.domain.entities.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemsDataDTO {

    private Product product;
    private String titleSnapshot;
    private Integer unitPriceCents;
    private Integer quantity;
    private Integer lineTotalCents;

}