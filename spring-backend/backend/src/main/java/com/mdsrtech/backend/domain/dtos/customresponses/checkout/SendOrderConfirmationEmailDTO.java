package com.mdsrtech.backend.domain.dtos.customresponses.checkout;

import com.mdsrtech.backend.domain.entities.Order;
import com.mdsrtech.backend.domain.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOrderConfirmationEmailDTO {

    private User user;
    private Order order;

}