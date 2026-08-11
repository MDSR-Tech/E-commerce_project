package com.mdsrtech.backend.services;

import com.mdsrtech.backend.domain.dtos.customresponses.orders.CancelOrderResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.orders.GetAllOrdersResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.orders.GetOrderDetailsResponseDTO;
import com.mdsrtech.backend.domain.entities.Order;
import com.mdsrtech.backend.domain.entities.User;
import jakarta.mail.MessagingException;

public interface OrderService {

    public void sendOrderCancellationEmail(User user, Order order);
    public GetAllOrdersResponseDTO getAllOrders(String email);
    public GetOrderDetailsResponseDTO getOrderDetails(String email, Long orderId);
    public CancelOrderResponseDTO cancelOrder(String email, Long orderId);

}
