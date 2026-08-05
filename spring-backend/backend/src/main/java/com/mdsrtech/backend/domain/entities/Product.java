package com.mdsrtech.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Objects;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id;

    @Column(name = "title", columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(name = "slug", columnDefinition = "TEXT", unique = true, nullable = false)
    private String slug;

    // Creates foreign key 'brand_id' in products table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", referencedColumnName = "id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Column(name = "currency", nullable = false, columnDefinition = "bpchar(3)")
    private String currency = "CAD";

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "sale_percent", nullable = true)
    private Integer salePercent;

    // Relationships
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductImage productImage;

    public Boolean isOnSale() {
        if (salePercent != null) {
            return true;
        }
        return category != null && category.getSalePercent() != null;
    }
    public Integer getEffectiveSalePercent() {
        if (salePercent != null) {
            return salePercent;
        }
        if (category != null && category.getSalePercent() != null) {
            return category.getSalePercent();
        }
        return null;
    }
    public Integer salePriceCents() {
        Integer percent = getEffectiveSalePercent();
        if (percent != null) {
            int discount = priceCents * percent / 100;
            return priceCents - discount;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product that = (Product) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}