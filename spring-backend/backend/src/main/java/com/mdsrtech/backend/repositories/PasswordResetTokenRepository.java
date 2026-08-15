package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    List<PasswordResetToken> findAllByUserIdAndUsedFalse(UUID userId);
}