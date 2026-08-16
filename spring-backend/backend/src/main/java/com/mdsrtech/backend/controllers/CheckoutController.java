package com.mdsrtech.backend.controllers;

import com.mdsrtech.backend.domain.dtos.customresponses.checkout.CreateCheckoutResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.checkout.GetSessionDetailsRequestDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.checkout.OrderDetailResponseDTO;
import com.mdsrtech.backend.services.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping(path = "/create-session")
    public ResponseEntity<CreateCheckoutResponseDTO> createCheckoutSession(Authentication authentication) {
        String email = authentication.getName();
        CreateCheckoutResponseDTO response = checkoutService.createCheckoutSession(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/session/{sessionId}")
    public ResponseEntity<OrderDetailResponseDTO> getSessionDetails(
            @PathVariable String sessionId,
            Authentication authentication) {
        String email = authentication.getName();
        GetSessionDetailsRequestDTO request = GetSessionDetailsRequestDTO.builder()
                .sessionId(sessionId)
                .build();
        return ResponseEntity.ok(checkoutService.getSessionDetails(request, email));
    }

    @PostMapping(path = "/webhook")
    public ResponseEntity<Map<String, Boolean>> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        return ResponseEntity.ok(checkoutService.stripeWebhook(payload, sigHeader));
    }

}