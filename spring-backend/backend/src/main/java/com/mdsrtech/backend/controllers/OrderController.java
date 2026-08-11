package com.mdsrtech.backend.controllers;

import com.mdsrtech.backend.domain.dtos.customresponses.orders.CancelOrderResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.orders.GetAllOrdersResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.orders.GetOrderDetailsResponseDTO;
import com.mdsrtech.backend.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping()
    public ResponseEntity<GetAllOrdersResponseDTO> getAllOrders(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getAllOrders(email));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<GetOrderDetailsResponseDTO> getOrderDetails(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getOrderDetails(email, id));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<CancelOrderResponseDTO> cancelOrder(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.cancelOrder(email, id));
    }

}