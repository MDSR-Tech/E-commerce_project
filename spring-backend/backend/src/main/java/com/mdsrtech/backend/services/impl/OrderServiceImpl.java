package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.domain.dtos.customresponses.orders.CancelOrderResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.orders.GetAllOrdersResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.orders.GetOrderDetailsResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.orders.OrderListItemDTO;
import com.mdsrtech.backend.domain.dtos.entities.OrderItemDTO;
import com.mdsrtech.backend.domain.entities.Order;
import com.mdsrtech.backend.domain.entities.OrderItem;
import com.mdsrtech.backend.domain.entities.User;
import com.mdsrtech.backend.repositories.OrderItemRepository;
import com.mdsrtech.backend.repositories.OrderRepository;
import com.mdsrtech.backend.repositories.UserRepository;
import com.mdsrtech.backend.services.OrderService;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public void sendOrderCancellationEmail(User user, Order order) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(user.getEmail());
            mimeMessageHelper.setSubject("Order Cancelled - MDSRTech #" + order.getId());
            mimeMessageHelper.setText(buildHtml(user, order), true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Failed to send order cancellation email: " + e.getMessage());
        }

    }

    @Transactional
    @Override
    public GetAllOrdersResponseDTO getAllOrders(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        List<Order> orders = orderRepository.findAllByUserIdOrderByPlacedAtDesc(user.getId());

        List<OrderListItemDTO> ordersList = orders.stream().map(order -> {
            List<OrderItem> orderItems = order.getOrderItems();
            OrderItem firstItem = orderItems.isEmpty() ? null : orderItems.getFirst();

            String previewImage;
            if (firstItem != null && firstItem.getProduct() != null && firstItem.getProduct().getProductImage() != null) {
                previewImage = firstItem.getProduct().getProductImage().getUrl();

            } else {
                previewImage = null;
            }

            return OrderListItemDTO.builder()
                    .id(order.getId())
                    .totalCents(order.getTotalCents())
                    .subtotalCents(order.getSubtotalCents())
                    .taxCents(order.getTaxCents())
                    .shippingCents(order.getShippingCents())
                    .currency(order.getCurrency())
                    .placedAt(order.getPlacedAt())
                    .itemCount(orderItems.size())
                    .previewImage(previewImage)
                    .firstItemName(firstItem != null ? firstItem.getTitleSnapshot() : null)
                    .build();

        }).toList();

        return GetAllOrdersResponseDTO.builder().orders(ordersList).build();

    }

    @Transactional
    @Override
    public GetOrderDetailsResponseDTO getOrderDetails(String email, Long orderId) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId()).orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItemDTO> items = order.getOrderItems().stream().map(orderItem -> {

            String productImage;
            String productSlug;

            Long productId = orderItem.getProduct() != null ? orderItem.getProduct().getId() : null;
            if (orderItem.getProduct() != null && orderItem.getProduct().getProductImage() != null) {
                productImage = orderItem.getProduct().getProductImage().getUrl();
                productSlug = orderItem.getProduct().getSlug();
            }
            else {
                productImage = null;
                productSlug = null;
            }

            return OrderItemDTO.builder()
                    .id(orderItem.getId())
                    .productId(productId)
                    .title(orderItem.getTitleSnapshot())
                    .quantity(orderItem.getQuantity())
                    .unitPriceCents(orderItem.getUnitPriceCents())
                    .lineTotalCents(orderItem.getLineTotalCents())
                    .imageUrl(productImage)
                    .productSlug(productSlug)
                    .build();

        }).toList();

        return GetOrderDetailsResponseDTO.builder()
                .id(orderId)
                .subtotalCents(order.getSubtotalCents())
                .taxCents(order.getTaxCents())
                .shippingCents(order.getShippingCents())
                .totalCents(order.getTotalCents())
                .currency(order.getCurrency())
                .placedAt(order.getPlacedAt())
                .items(items)
                .build();

    }

    @Transactional
    @Override
    public CancelOrderResponseDTO cancelOrder(String email, Long orderId) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId()).orElseThrow(() -> new RuntimeException("Order not found"));

        sendOrderCancellationEmail(user, order);

        orderItemRepository.deleteByOrderId(orderId);
        orderRepository.deleteById(orderId);

        return CancelOrderResponseDTO.builder()
                .message("Order has been cancelled")
                .orderId(order.getId())
                .build();

    }

    private String buildHtml(User user, Order order) {
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
            <h2 style="color: #dc2626;">Order Cancelled</h2>

            <p style="color: #4b5563; font-size: 16px;">
                Hi %s,
            </p>
            <p style="color: #4b5563; font-size: 16px;">
                Your order has been cancelled as requested. Here&apos;s a summary of the cancelled order:
            </p>

            <div style="background-color: #fef2f2; border-radius: 8px; padding: 20px; margin: 20px 0; border: 1px solid #fecaca;">
                <p style="margin: 0; color: #6b7280; font-size: 14px;">Cancelled Order Number</p>
                <p style="margin: 5px 0 0; color: #dc2626; font-size: 20px; font-weight: bold;">#%d</p>
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
                <div style="display: flex; justify-content: space-between; font-size: 18px; font-weight: bold;">
                    <span style="color: #1f2937;">Refund Amount:</span>
                    <span style="color: #dc2626;">$%.2f CAD</span>
                </div>
            </div>

            <p style="color: #4b5563; font-size: 14px; margin-top: 30px;">
                If you paid for this order, your refund will be processed within 5-10 business days.
            </p>

            <p style="color: #4b5563; font-size: 14px;">
                If you have any questions, please don&apos;t hesitate to contact us.
            </p>

            <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;" />
            <p style="color: #9ca3af; font-size: 12px; text-align: center;">
                © 2025 MDSRTech. All rights reserved.<br/>
                This is a demonstration project for educational purposes only.
            </p>
        </div>
        """,
                user.getFullName(),
                order.getId(),
                itemsHtml,
                order.getTotalCents() / 100.0);
    }
}
