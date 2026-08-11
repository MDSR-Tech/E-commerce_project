package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.OrderItem;
import org.springframework.data.repository.Repository;

public interface OrderItemRepository extends Repository<OrderItem, Long> {
    void deleteByOrderId(Long orderId);
}
