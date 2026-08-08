package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.domain.dtos.customresponses.cart.*;
import com.mdsrtech.backend.domain.dtos.entities.CartItemDTO;
import com.mdsrtech.backend.domain.dtos.entities.ProductDTO;
import com.mdsrtech.backend.domain.entities.*;
import com.mdsrtech.backend.mapper.Mapper;
import com.mdsrtech.backend.repositories.*;
import com.mdsrtech.backend.services.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final Mapper<Product, ProductDTO> productMapper;
    private final PromoCodeRepository promoCodeRepository;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public Cart getOrCreateCart(User user) {

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

    }

    @Override
    public Integer getEffectivePrice(Product product) {

        if (product.isOnSale() && product.salePriceCents() != null) {
            return product.salePriceCents();
        } else {
            return product.getPriceCents();
        }

    }

    @Transactional
    @Override
    public GetCartResponseDTO getCart(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = getOrCreateCart(user);
        List<CartItemDTO> items = new  ArrayList<>();
        int subtotalCents = 0;
        int originalSubtotalCents = 0;
        int totalItems = 0;
        for (CartItem item : cart.getCartItems()) {
            if (item.getProduct() != null && item.getProduct().getIsActive()) {

                int effectivePrice = getEffectivePrice(item.getProduct());
                int lineTotal = effectivePrice * item.getQuantity();
                int originalLineTotal = item.getProduct().getPriceCents() * item.getQuantity();

                subtotalCents += lineTotal;
                originalSubtotalCents += originalLineTotal;
                totalItems += item.getQuantity();

                ProductDTO product = productMapper.mapFromEntityToDTO(item.getProduct());

                items.add(CartItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .quantity(item.getQuantity())
                        .unitPriceCents(effectivePrice)
                        .originalPriceCents(item.getProduct().getPriceCents())
                        .lineTotalCents(lineTotal)
                        .originalLineTotalCents(originalLineTotal)
                        .addedAt(item.getAddedAt())
                        .product(product)
                        .build());

            }
        }

        double taxRate = 0.13;
        int taxCents = (int) (subtotalCents * taxRate);
        int shippingCents = subtotalCents >= 10000 ? 0 : 999;
        int totalCents = subtotalCents + taxCents + shippingCents;
        int saleSavingCents = originalSubtotalCents - subtotalCents;

        return GetCartResponseDTO.builder()
                .cartId(cart.getId())
                .items(items)
                .itemCount(items.size())
                .totalItems(totalItems)
                .subtotalCents(subtotalCents)
                .originalSubtotalCents(originalSubtotalCents)
                .saleSavingCents(saleSavingCents)
                .taxCents(taxCents)
                .taxRate(taxRate)
                .shippingCents(shippingCents)
                .totalCents(totalCents)
                .currency("CAD")
                .build();

    }

    @Transactional
    @Override
    public GetCartCountResponseDTO getCartCount(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return GetCartCountResponseDTO.builder()
                    .count(0)
                    .totalItems(0)
                    .build();
        }

        int count =  cart.getCartItems().size();
        int totalItems = cart.getCartItems().stream().mapToInt(CartItem::getQuantity).sum();

        return GetCartCountResponseDTO.builder()
                .count(count)
                .totalItems(totalItems)
                .build();

    }

    @Transactional
    @Override
    public AddToCartResponseDTO addToCart(String email, CartItemRequestDTO request) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (productId == null) {
            throw new RuntimeException("Product Id is required");
        }
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Product product = productRepository.findByIdAndIsActiveTrue(productId).orElseThrow(() -> new RuntimeException("Product not found or inactive"));
        if (quantity > product.getStock()) {
            throw new RuntimeException("Only " + product.getStock() + " items available");
        }

        Cart cart = getOrCreateCart(user);
        Integer effectivePrice = getEffectivePrice(product);

        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElse(null);
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if  (newQuantity > product.getStock()) {
                throw new RuntimeException("Cannot add more. Only " + product.getStock() + " items available, you have " + existingItem.getQuantity() + " in your cart.");
            }

            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPriceCents(effectivePrice);
            cartItemRepository.save(existingItem);
            return AddToCartResponseDTO.builder()
                    .message("Cart updated")
                    .productId(productId)
                    .quantity(existingItem.getQuantity())
                    .itemId(existingItem.getId())
                    .action(Action.updated)
                    .build();

        }
        else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPriceCents(effectivePrice)
                    .build();
            cartItemRepository.save(cartItem);
            return AddToCartResponseDTO.builder()
                    .message("Added to cart")
                    .productId(productId)
                    .quantity(quantity)
                    .itemId(cartItem.getId())
                    .action(Action.added)
                    .build();
        }

    }

    @Transactional
    @Override
    public UpdateItemResponseDTO updateCartItem(String email, CartItemRequestDTO request) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        if (productId == null) {
            throw new RuntimeException("Product Id is required");
        }
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElseThrow(() -> new RuntimeException("Item not found in cart"));

        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (quantity > product.getStock())
            throw new RuntimeException("Only " + product.getStock() + " items available");

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return UpdateItemResponseDTO.builder()
                .message("Cart item updated successfully")
                .productId(productId)
                .quantity(quantity)
                .build();

    }

    @Transactional
    @Override
    public RemoveFromCartResponseDTO removeFromCart(String email, RemoveRequestDTO request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Long productId = request.getProductId();

        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("Cart is null"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElseThrow(() -> new IllegalArgumentException("Product not found in cart"));

        cartItemRepository.delete(item);

        return RemoveFromCartResponseDTO.builder()
                .message("Product removed from cart")
                .productId(productId)
                .build();

    }

    @Transactional
    @Override
    public ClearCartResponseDTO clearCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return ClearCartResponseDTO.builder().message("Cart is already empty").build();
        }

        cartItemRepository.deleteByCartId(cart.getId());
        return ClearCartResponseDTO.builder().message("Cart cleared successfully").build();

    }

    @Transactional
    @Override
    public ApplyPromoResponseDTO applyPromo(String email, PromoRequestDTO promoRequestDTO) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (promoRequestDTO.getPromoCode() == null || promoRequestDTO.getPromoCode().isBlank()) {
            throw new IllegalArgumentException("Promo code is required");
        }

        String code = promoRequestDTO.getPromoCode().strip().toUpperCase();

        PromoCode promo = promoCodeRepository.findByCodeAndIsActiveTrue(code).orElseThrow(() -> new RuntimeException("Invalid promo code"));

        return ApplyPromoResponseDTO.builder()
                .message("Promo code applied")
                .promoCode(promo.getCode())
                .discountPercent(promo.getDiscountPercent())
                .build();

    }

}