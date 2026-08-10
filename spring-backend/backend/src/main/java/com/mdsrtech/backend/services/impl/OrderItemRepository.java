package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.domain.entities.OrderItem;
import org.springframework.data.repository.Repository;

import java.util.Optional;

interface OrderItemRepository extends Repository<OrderItem, Long> {
    Optional<OrderItem> deleteByOrderId(Long orderId);
}
