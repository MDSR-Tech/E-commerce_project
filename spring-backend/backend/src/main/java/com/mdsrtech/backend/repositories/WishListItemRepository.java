package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface WishListItemRepository extends JpaRepository<WishListItem, Long> {

    Optional<WishListItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

}