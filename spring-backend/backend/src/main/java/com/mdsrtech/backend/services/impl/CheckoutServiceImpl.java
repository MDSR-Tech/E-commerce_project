package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.domain.dtos.customresponses.checkout.CreateCheckoutResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.checkout.GetSessionDetailsRequestDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.checkout.OrderDetailResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.checkout.OrderItemsDataDTO;
import com.mdsrtech.backend.domain.dtos.entities.OrderDTO;
import com.mdsrtech.backend.domain.entities.*;
import com.mdsrtech.backend.repositories.*;
import com.mdsrtech.backend.services.CheckoutService;
import com.google.gson.Gson;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final JavaMailSender javaMailSender;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    @Override
    public CreateCheckoutResponseDTO createCheckoutSession(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Cart not found"));
        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }


        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (var cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null || !product.getIsActive()) {
                continue;
            }

            int unitPrice = product.isOnSale() && product.salePriceCents() != null ? product.salePriceCents() : product.getPriceCents();
            String imageUrl = product.getProductImage() != null ? product.getProductImage().getUrl() : null;

            SessionCreateParams.LineItem.PriceData.ProductData.Builder productDataBuilder = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName(product.getTitle())
                    .setDescription(product.getBrand() != null ? product.getBrand().getName() : null);

            if (imageUrl != null) {
                productDataBuilder.addImage(imageUrl);
            }

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("cad")
                                    .setUnitAmount((long) unitPrice)
                                    .setProductData(productDataBuilder.build())
                                    .build()
                    )
                    .setQuantity((long) cartItem.getQuantity())
                    .build();

            lineItems.add(lineItem);
        }

        if (lineItems.isEmpty()) {
            throw new RuntimeException("No valid items in cart");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addAllLineItem(lineItems)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/checkout/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/cart")
                .setCustomerEmail(user.getEmail())
                .putMetadata("user_id", user.getId().toString())
                .setShippingAddressCollection(
                        SessionCreateParams.ShippingAddressCollection.builder()
                                .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.CA)
                                .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.US)
                                .build()
                )
                .addShippingOption(
                        SessionCreateParams.ShippingOption.builder()
                                .setShippingRateData(
                                        SessionCreateParams.ShippingOption.ShippingRateData.builder()
                                                .setType(SessionCreateParams.ShippingOption.ShippingRateData.Type.FIXED_AMOUNT)
                                                .setFixedAmount(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount.builder()
                                                                .setAmount(0L)
                                                                .setCurrency("cad")
                                                                .build()
                                                )
                                                .setDisplayName("Free Shipping")
                                                .setDeliveryEstimate(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.builder()
                                                                .setMinimum(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum.builder()
                                                                                .setUnit(SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum.Unit.BUSINESS_DAY)
                                                                                .setValue(5L)
                                                                                .build()
                                                                )
                                                                .setMaximum(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum.builder()
                                                                                .setUnit(SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum.Unit.BUSINESS_DAY)
                                                                                .setValue(7L)
                                                                                .build()
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .addShippingOption(
                        SessionCreateParams.ShippingOption.builder()
                                .setShippingRateData(
                                        SessionCreateParams.ShippingOption.ShippingRateData.builder()
                                                .setType(SessionCreateParams.ShippingOption.ShippingRateData.Type.FIXED_AMOUNT)
                                                .setFixedAmount(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount.builder()
                                                                .setAmount(1499L)
                                                                .setCurrency("cad")
                                                                .build()
                                                )
                                                .setDisplayName("Express Shipping")
                                                .setDeliveryEstimate(
                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.builder()
                                                                .setMinimum(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum.builder()
                                                                                .setUnit(SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Minimum.Unit.BUSINESS_DAY)
                                                                                .setValue(1L)
                                                                                .build()
                                                                )
                                                                .setMaximum(
                                                                        SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum.builder()
                                                                                .setUnit(SessionCreateParams.ShippingOption.ShippingRateData.DeliveryEstimate.Maximum.Unit.BUSINESS_DAY)
                                                                                .setValue(3L)
                                                                                .build()
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .setAutomaticTax(
                        SessionCreateParams.AutomaticTax.builder().setEnabled(false).build()
                )
                .build();

        Session checkoutSession;
        try {
            checkoutSession = Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e.getMessage());
        }

        return CreateCheckoutResponseDTO.builder()
                .checkoutUrl(checkoutSession.getUrl())
                .sessionId(checkoutSession.getId())
                .build();

    }

    @Transactional
    @Override
    public OrderDetailResponseDTO getSessionDetails(GetSessionDetailsRequestDTO sessionId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Session session;
        try {
            SessionRetrieveParams params = SessionRetrieveParams.builder()
                    .addExpand("line_items")
                    .addExpand("payment_intent")
                    .build();
            session = Session.retrieve(sessionId.getSessionId(), params, null);
        } catch (StripeException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (!user.getId().toString().equals(session.getMetadata().get("user_id"))) {
            throw new RuntimeException("Unauthorized");
        }
        if (!session.getPaymentStatus().equals("paid")) {
            throw new RuntimeException("Payment not completed");
        }

        String paymentIntentId = session.getPaymentIntent();

        Optional<Payment> existingPayment = paymentRepository.findByProviderPaymentId(paymentIntentId);
        if (existingPayment.isPresent()) {
            Optional<Order> exisingOrder = orderRepository.findByPaymentId(existingPayment.get().getId());
            if (exisingOrder.isPresent()) {
                return OrderDetailResponseDTO.builder()
                        .success(true)
                        .orderId(exisingOrder.get().getId())
                        .alreadyProcessed(true)
                        .build();
            }
        }

        Order order = createOrderFromSession(session, user);

        return OrderDetailResponseDTO.builder()
                .success(true)
                .orderId(order.getId())
                .alreadyProcessed(false)
                .build();

    }

    @Transactional
    @Override
    public Map<String, Boolean> stripeWebhook(String payload, String sigHeader) {
        Event event;

        if (webhookSecret != null && !webhookSecret.isBlank()) {
            try {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } catch (SignatureVerificationException e) {
                throw new RuntimeException("Invalid signature");
            } catch (Exception e) {
                throw new RuntimeException("Invalid payload");
            }
        } else {
            // No webhook secret configured - skip signature verification (development only)
            try {
                event = new Gson().fromJson(payload, Event.class);
            } catch (Exception e) {
                throw new RuntimeException("Invalid payload");
            }
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Unable to deserialize event"));

            String userIdStr = session.getMetadata() != null ? session.getMetadata().get("user_id") : null;

            if (userIdStr != null) {
                try {
                    SessionRetrieveParams params = SessionRetrieveParams.builder()
                            .addExpand("line_items")
                            .build();
                    Session fullSession = Session.retrieve(session.getId(), params, null);

                    User user = userRepository.findById(UUID.fromString(userIdStr))
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    createOrderFromSession(fullSession, user);
                } catch (StripeException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }

        return Map.of("received", true);
    }

    @Transactional
    public Order createOrderFromSession(Session session, User user) {

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Cart not found"));
        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        int subtotalCents = 0;
        List<OrderItemsDataDTO> orderItemsData = new ArrayList<>();
        for (var cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                continue;
            }

            int unitPrice = 0;

            if (product.isOnSale() && product.salePriceCents() != null) {
                unitPrice = product.salePriceCents();
            }
            else {
                unitPrice = product.getPriceCents();
            }

            int lineTotal = unitPrice * cartItem.getQuantity();
            subtotalCents += lineTotal;

            orderItemsData.add(OrderItemsDataDTO.builder()
                    .product(product)
                    .titleSnapshot(product.getTitle())
                    .unitPriceCents(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineTotalCents(lineTotal)
                    .build());

        }

        int shippingCents = session.getShippingCost() != null && session.getShippingCost().getAmountTotal() != null
                ? session.getShippingCost().getAmountTotal().intValue() : 0;
        int taxCents = (int) ((subtotalCents + shippingCents) * 0.13);
        int totalCents = subtotalCents + shippingCents + taxCents;

        String paymentIntentId = session.getPaymentIntent();
        Payment payment = Payment.builder()
                .provider("stripe")
                .providerPaymentId(paymentIntentId)
                .status(PaymentStatus.succeeded)
                .amountCents(totalCents)
                .currency("CAD")
                .rawResponse("{\"session_id\":\"" + session.getId() + "\"}")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        Order order = Order.builder()
                .user(user)
                .subtotalCents(subtotalCents)
                .shippingCents(shippingCents)
                .taxCents(taxCents)
                .totalCents(totalCents)
                .currency("CAD")
                .payment(savedPayment)
                .build();

        Order savedOrder = orderRepository.save(order);
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (OrderItemsDataDTO itemData : orderItemsData) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(itemData.getProduct())
                    .titleSnapshot(itemData.getTitleSnapshot())
                    .unitPriceCents(itemData.getUnitPriceCents())
                    .quantity(itemData.getQuantity())
                    .lineTotalCents(itemData.getLineTotalCents())
                    .build();
            savedOrderItems.add(orderItemRepository.save(orderItem));
        }
        savedOrder.setOrderItems(savedOrderItems);

        cartItemRepository.deleteByCartId(cart.getId());
        try {
            sendOrderConfirmationEmail(user, savedOrder);
            System.out.println("DEBUG: sendOrderConfirmationEmail returned normally");
        } catch (Exception e) {
            System.out.println("DEBUG: sendOrderConfirmationEmail threw: " + e);
        }
        return savedOrder;

    }

    private void sendOrderConfirmationEmail(User user, Order order) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(user.getEmail());
            mimeMessageHelper.setSubject("Order Confirmation - MDSRTech #" + order.getId());
            mimeMessageHelper.setText(buildOrderConfirmationHtml(user, order), true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    private String buildOrderConfirmationHtml(User user, Order order) {
        StringBuilder itemsHtml = new StringBuilder();
        for (OrderItem item : order.getOrderItems()) {
            itemsHtml.append(String.format("""
            <tr>
                <td style="padding: 12px; border-bottom: 1px solid #e5e7eb;">%s</td>
                <td style="padding: 12px; border-bottom: 1px solid #e5e7eb; text-align: center;">%d</td>
                <td style="padding: 12px; border-bottom: 1px solid #e5e7eb; text-align: right;">$%.2f</td>
            </tr>
            """,
                    item.getTitleSnapshot(),
                    item.getQuantity(),
                    item.getLineTotalCents() / 100.0));
        }

        return String.format("""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <h1 style="color: #2563eb; text-align: center;">MDSRTech</h1>
            <h2 style="color: #1f2937;">Thank you for your order!</h2>

            <p style="color: #4b5563; font-size: 16px;">
                Hi %s,
            </p>
            <p style="color: #4b5563; font-size: 16px;">
                We've received your order and it's being processed. Here's your order summary:
            </p>

            <div style="background-color: #f9fafb; border-radius: 8px; padding: 20px; margin: 20px 0;">
                <p style="margin: 0; color: #6b7280; font-size: 14px;">Order Number</p>
                <p style="margin: 5px 0 0; color: #1f2937; font-size: 20px; font-weight: bold;">#%d</p>
            </div>

            <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                <thead>
                    <tr style="background-color: #f3f4f6;">
                        <th style="padding: 12px; text-align: left;">Item</th>
                        <th style="padding: 12px; text-align: center;">Qty</th>
                        <th style="padding: 12px; text-align: right;">Total</th>
                    </tr>
                </thead>
                <tbody>
                    %s
                </tbody>
            </table>

            <div style="border-top: 2px solid #e5e7eb; padding-top: 15px; margin-top: 15px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                    <span style="color: #6b7280;">Subtotal:</span>
                    <span style="color: #1f2937;">$%.2f</span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                    <span style="color: #6b7280;">Shipping:</span>
                    <span style="color: #1f2937;">$%.2f</span>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                    <span style="color: #6b7280;">Tax (HST 13%%):</span>
                    <span style="color: #1f2937;">$%.2f</span>
                </div>
                <div style="display: flex; justify-content: space-between; font-size: 18px; font-weight: bold; margin-top: 10px; padding-top: 10px; border-top: 1px solid #e5e7eb;">
                    <span style="color: #1f2937;">Total:</span>
                    <span style="color: #2563eb;">$%.2f CAD</span>
                </div>
            </div>

            <p style="color: #4b5563; font-size: 14px; margin-top: 30px;">
                You'll receive another email when your order ships.
            </p>

            <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;" />
            <p style="color: #9ca3af; font-size: 12px; text-align: center;">
                © 2025 MDSRTech. All rights reserved.
            </p>
        </div>
        """,
                user.getFullName(),
                order.getId(),
                itemsHtml,
                order.getSubtotalCents() / 100.0,
                order.getShippingCents() / 100.0,
                order.getTaxCents() / 100.0,
                order.getTotalCents() / 100.0);
    }

}