package customDesign.E_commerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderItemResponse(Long productId, String productName, String description,
                                String specifications,
                                String imageUrl, Long categoryId, String categoryName,
                                Long sellerId, String sellerName, String sellerEmail,
                                BigDecimal unitPrice, Integer quantity, BigDecimal subtotal,
                                Integer estimatedProductionDays,
                                Integer estimatedProductionHours, Integer actualProductionHours,
                                Instant productionStartedAt, Instant productionCompletedAt) {}
