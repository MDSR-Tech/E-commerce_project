package com.mdsrtech.backend.services;

import com.mdsrtech.backend.domain.dtos.customresponses.checkout.CreateCheckoutResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.checkout.GetSessionDetailsRequestDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.checkout.OrderDetailResponseDTO;
import com.mdsrtech.backend.domain.dtos.entities.OrderDTO;
import com.mdsrtech.backend.domain.entities.Order;
import com.mdsrtech.backend.domain.entities.User;
import com.stripe.model.checkout.Session;

import java.util.Map;

public interface CheckoutService {

    CreateCheckoutResponseDTO createCheckoutSession(String email);
    OrderDetailResponseDTO getSessionDetails(GetSessionDetailsRequestDTO sessionId, String email);

    Map<String, Boolean> stripeWebhook(String payload, String sigHeader);

}
