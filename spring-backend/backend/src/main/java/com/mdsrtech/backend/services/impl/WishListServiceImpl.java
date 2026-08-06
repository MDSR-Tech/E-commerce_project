package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.config.security.JwtService;
import com.mdsrtech.backend.domain.dtos.customresponses.wishlists.*;
import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import com.mdsrtech.backend.domain.entities.Product;
import com.mdsrtech.backend.domain.entities.User;
import com.mdsrtech.backend.domain.entities.WishList;
import com.mdsrtech.backend.domain.entities.WishListItem;
import com.mdsrtech.backend.mapper.Mapper;
import com.mdsrtech.backend.repositories.ProductRepository;
import com.mdsrtech.backend.repositories.UserRepository;
import com.mdsrtech.backend.repositories.WishListItemRepository;
import com.mdsrtech.backend.repositories.WishListRepository;
import com.mdsrtech.backend.services.WishListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final Mapper<Product, ProductDTO> productMapper;
    private final ProductRepository productRepository;
    private final WishListItemRepository wishListItemRepository;

    @Transactional
    @Override
    public WishList getOrCreateWishList(User user) {

        return wishListRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    WishList newWishList = WishList.builder().user(user).build();
                    return wishListRepository.save(newWishList);
                });

    }

    @Transactional
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

    @Transactional
    @Override
    public GetProductIdsResponseDTO getWishListProductIds(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WishList wishList = getOrCreateWishList(user);

        List<Long> productIds = new ArrayList<>();
        for (WishListItem item : wishList.getWishlistItems()) {
            productIds.add(item.getProduct().getId());
        }
//        List<Long> productIds = wishList.getWishlistItems().stream()
//                .map(item -> item.getProduct().getId())
//                .toList();


        return GetProductIdsResponseDTO.builder()
                .productIds(productIds)
                .build();

    }

    @Transactional
    @Override
    public WishListAddResponseDTO addToWishList(String email, ProductIdRequestDTO request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getProductId() == null) {
            throw new RuntimeException("Product id is required");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        WishList wishlist = getOrCreateWishList(user);

        Optional<WishListItem> exists = wishListItemRepository.findByWishListIdAndProductId(
                wishlist.getId(),
                request.getProductId()
        );

        if (exists.isPresent()) {
            return WishListAddResponseDTO.builder()
                    .message("Product already in wishlist")
                    .productId(request.getProductId())
                    .build();
        }

        WishListItem item = WishListItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();

        wishListItemRepository.save(item);
        return WishListAddResponseDTO.builder()
                .message("Product added to wishlist")
                .productId(request.getProductId())
                .itemId(item.getId())
                .build();

    }

    @Transactional
    @Override
    public RemoveItemFromWishListDTO removeFromWishList(String email, ProductIdRequestDTO request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getProductId() == null) {
            throw new RuntimeException("Product id is required");
        }

        WishList wishlist = wishListRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));;
        WishListItem item = wishListItemRepository.findByWishListIdAndProductId(wishlist.getId(), request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not in wishlist"));
        wishListItemRepository.delete(item);

        return RemoveItemFromWishListDTO.builder()
                .message("Removed from wishlist")
                .productId(request.getProductId())
                .build();

    }

    @Transactional
    @Override
    public ToggleWishListDTO toggleWishList(String email, ProductIdRequestDTO request) {

        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getProductId() == null) {
            throw new RuntimeException("Product id is required");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        WishList wishlist = getOrCreateWishList(user);

        Optional<WishListItem> itemExists = wishListItemRepository.findByWishListIdAndProductId(wishlist.getId(), request.getProductId());

        if (itemExists.isPresent()) {
            wishListItemRepository.delete(itemExists.get());
            return ToggleWishListDTO.builder()
                    .message("Removed from wishlist")
                    .productId(request.getProductId())
                    .action(Action.removed)
                    .inWishlist(false)
                    .build();
        }
        else {
            WishListItem item = WishListItem.builder()
                    .wishlist(wishlist)
                    .product(product)
                    .build();
            wishListItemRepository.save(item);
            return ToggleWishListDTO.builder()
                    .message("Added to wishlist")
                    .productId(request.getProductId())
                    .itemId(item.getId())
                    .action(Action.added)
                    .inWishlist(true)
                    .build();
        }

    }

    @Transactional
    @Override
    public InWishListResponseDTO checkInWishList(String email, Long productId) {

        User user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<WishList> wishlist = wishListRepository.findByUserId(user.getId());
        if (wishlist.isEmpty()) {
            return InWishListResponseDTO.builder().inWishlist(false).build();
        }

        boolean exists = wishListItemRepository.findByWishListIdAndProductId(
                wishlist.get().getId(),
                productId
        ).isPresent();
        return InWishListResponseDTO.builder()
                .inWishlist(exists)
                .build();

    }
}