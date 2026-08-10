package com.mdsrtech.backend.domain.dtos.customresponses.orders;

import com.mdsrtech.backend.domain.entities.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllOrdersResponseDTO {

    private List<Order> orders;

}