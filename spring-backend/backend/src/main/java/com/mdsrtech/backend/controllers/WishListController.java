package com.mdsrtech.backend.controllers;

import com.mdsrtech.backend.domain.dtos.customresponses.wishlist.*;
import com.mdsrtech.backend.services.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;

    @GetMapping
    public ResponseEntity<GetWishListResponseDTO> getWishList(Authentication authentication) {
        return ResponseEntity.ok(wishListService.getWishList(authentication.getName()));
    }

    @GetMapping(path = "/ids")
    public ResponseEntity<GetProductIdsResponseDTO> getWishListProductIds(Authentication authentication) {
        return ResponseEntity.ok(wishListService.getWishListProductIds(authentication.getName()));
    }

    @PostMapping(path = "/add")
    public ResponseEntity<WishListAddResponseDTO> addToWishList(Authentication authentication, @RequestBody ProductIdRequestDTO request) {

        WishListAddResponseDTO response = wishListService.addToWishList(authentication.getName(), request);
        HttpStatus status = response.getItemId() != null ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);

    }

    @DeleteMapping(path = "/remove")
    public ResponseEntity<RemoveItemFromWishListDTO> removeFromWishList(Authentication authentication, @RequestBody ProductIdRequestDTO request) {
        return ResponseEntity.ok(wishListService.removeFromWishList(authentication.getName(), request));
    }

    @PostMapping(path = "/toggle")
    public ResponseEntity<ToggleWishListDTO> toggleWishList(Authentication authentication, @RequestBody ProductIdRequestDTO request) {
        return ResponseEntity.ok(wishListService.toggleWishList(authentication.getName(), request));
    }

    @GetMapping(path = "/check/{id}")
    public ResponseEntity<InWishListResponseDTO> checkInWishList(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(wishListService.checkInWishList(authentication.getName(), id));
    }

}