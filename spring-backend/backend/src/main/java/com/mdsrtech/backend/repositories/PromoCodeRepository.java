package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeAndIsActiveTrue(String code);
}