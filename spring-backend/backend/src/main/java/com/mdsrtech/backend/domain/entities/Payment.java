package com.mdsrtech.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "provider", columnDefinition = "TEXT", nullable = false)
    private String provider = "stripe";

    @Column(name = "provider_payment_id", columnDefinition = "TEXT", nullable = false, unique = true)
    private String providerPaymentId;

    @Column(name = "status", length = 50, nullable = false)
    private String status = "succeeded";

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(name = "currency", columnDefinition = "bpchar(3)", nullable = false)
    private String currency = "CAD";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response", columnDefinition = "jsonb")
    private String rawResponse;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment that = (Payment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}