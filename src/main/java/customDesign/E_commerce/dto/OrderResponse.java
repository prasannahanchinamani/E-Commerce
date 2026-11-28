package customDesign.E_commerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long buyerId,
        Long sellerId,
        String type,
        Integer quantity,
        BigDecimal amount,
        BigDecimal commissionRate,
        BigDecimal commissionAmount,
        BigDecimal sellerPayout,
        String status,
        AddressResponse address,
        Instant createdAt,
        java.util.List<OrderItemResponse> items,
        String paymentStatus,
        Instant expectedDeliveryAt,
        Instant deliveredAt,
        String cancellationReason,
        Instant cancelledAt
) {}
