package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.config.security.JwtService;
import com.mdsrtech.backend.domain.dtos.customresponses.wishlists.*;
import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import com.mdsrtech.backend.domain.entities.Product;
import com.mdsrtech.backend.domain.entities.User;
import com.mdsrtech.backend.domain.entities.WishList;
import com.mdsrtech.backend.domain.entities.WishListItem;
import com.mdsrtech.backend.mapper.Mapper;
import com.mdsrtech.backend.repositories.UserRepository;
import com.mdsrtech.backend.repositories.WishListRepository;
import com.mdsrtech.backend.services.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final Mapper<Product, ProductDTO> productMapper;

    @Override
    public WishList getOrCreateWishList(User user) {

        return wishListRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    WishList newWishList = WishList.builder().user(user).build();
                    return wishListRepository.save(newWishList);
                });

    }

    @Override
    public GetWishListResponseDTO getWishList(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WishList wishList = getOrCreateWishList(user);

        List<WishListItemDTO> items = new ArrayList<>();
        for (WishListItem item : wishList.getWishlistItems()) {

            if (item.getProduct() != null && item.getProduct().getIsActive()) {
                ProductDTO productDTO = productMapper.mapFromEntityToDTO(item.getProduct());
                items.add(WishListItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .product(productDTO)
                        .build());
            }
        }

        return GetWishListResponseDTO.builder()
                .wishlistId(wishList.getId())
                .items(items)
                .count(items.size())
                .build();
    }

    @Override
    public GetProductIdsResponseDTO getWishListProductIds(String email) {
        return null;
    }

    @Override
    public WishListAddResponseDTO addToWishList(String email, ProductIdRequestDTO request) {
        return null;
    }

    @Override
    public RemoveItemFromWishListDTO removeFromWishList(String email, ProductIdRequestDTO request) {
        return null;
    }

    @Override
    public ToggleWishListDTO toggleWishList(String email, ProductIdRequestDTO request) {
        return null;
    }

    @Override
    public InWishListResponseDTO checkInWishList(String email, Long productId) {
        return null;
    }
}