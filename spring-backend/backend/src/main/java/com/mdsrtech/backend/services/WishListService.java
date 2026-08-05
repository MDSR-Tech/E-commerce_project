package com.mdsrtech.backend.services;

import com.mdsrtech.backend.domain.dtos.customresponses.wishlists.*;
import com.mdsrtech.backend.domain.entities.WishList;

import java.util.Map;
import java.util.UUID;

public interface WishListService {

    public WishList getOrCreateWishList(UUID userId);
    public GetWishListResponseDTO getWishList(String email);
    public GetProductIdsResponseDTO getWishListProductIds(String email);
    public WishListAddResponseDTO addToWishList(String email, ProductIdRequestDTO request);
    public RemoveItemFromWishListDTO removeFromWishList(String email, ProductIdRequestDTO request);
    public ToggleWishListDTO toggleWishList(String email, ProductIdRequestDTO request);
    public InWishListResponseDTO checkInWishList(String email, Long productId);


}
