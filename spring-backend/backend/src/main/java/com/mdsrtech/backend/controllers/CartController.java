package com.mdsrtech.backend.controllers;

import com.mdsrtech.backend.domain.dtos.customresponses.cart.*;
import com.mdsrtech.backend.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<GetCartResponseDTO> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    @GetMapping(path = "/count")
    public ResponseEntity<GetCartCountResponseDTO> getCartCount(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCartCount(authentication.getName()));
    }

    @PostMapping(path = "/add")
    public ResponseEntity<AddToCartResponseDTO> addToCart(Authentication authentication, @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.addToCart(authentication.getName(), request));
    }

    @PutMapping(path = "/update")
    public ResponseEntity<UpdateItemResponseDTO> updateCartItem(Authentication authentication, @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateCartItem(authentication.getName(), request));
    }

    @DeleteMapping(path = "/remove")
    public ResponseEntity<RemoveFromCartResponseDTO> removeFromCart(Authentication authentication, @RequestBody RemoveRequestDTO request) {
        return ResponseEntity.ok(cartService.removeFromCart(authentication.getName(), request));
    }

    @DeleteMapping(path = "/clear")
    public ResponseEntity<ClearCartResponseDTO> clearCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.clearCart(authentication.getName()));
    }

    @PostMapping(path = "/apply-promo")
    public ResponseEntity<ApplyPromoResponseDTO> applyPromo(Authentication authentication, @RequestBody PromoRequestDTO promoCode) {
        return ResponseEntity.ok(cartService.applyPromo(authentication.getName(), promoCode));
    }

}