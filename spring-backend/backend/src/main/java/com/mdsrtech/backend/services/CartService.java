package com.mdsrtech.backend.services;


import com.mdsrtech.backend.domain.dtos.customresponses.cart.*;
import com.mdsrtech.backend.domain.entities.Cart;
import com.mdsrtech.backend.domain.entities.Product;
import com.mdsrtech.backend.domain.entities.User;

public interface CartService {

    public Cart getOrCreateCart(User user);
    public Integer getEffectivePrice(Product product);
    public GetCartResponseDTO getCart(String email);
    public GetCartCountResponseDTO getCartCount(String email);
    public AddToCartResponseDTO addToCart(String email, CartItemRequestDTO request);
    public UpdateItemResponseDTO updateCartItem(String email, CartItemRequestDTO request);
    public RemoveFromCartResponseDTO removeFromCart(String email, RemoveRequestDTO request);
    public ClearCartResponseDTO clearCart(String email);
    public ApplyPromoResponseDTO applyPromo(String email, PromoRequestDTO promoCode);

}
