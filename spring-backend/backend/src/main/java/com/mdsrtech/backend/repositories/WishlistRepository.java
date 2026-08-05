package com.mdsrtech.backend.repositories;

import com.mdsrtech.backend.domain.entities.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<WishList, Long> {

}