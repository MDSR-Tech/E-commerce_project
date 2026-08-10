package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(Long id, UUID userId);
    List<Order> findAllByUserIdOrderByPlacedAtDesc(UUID userId);

}