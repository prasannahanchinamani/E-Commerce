package customDesign.E_commerce.dto;

import java.math.BigDecimal;
import java.util.List;

public record ListingResponse(
        Long id,
        String title,
        Long categoryId,
        Long designTypeId,
        BigDecimal price,
        BigDecimal bulkPrice,
        Integer minBulkQty,
        Integer daysToMake,
        List<String> images,
        SellerSummary seller,
        Double rating,
        String status
) {
    public record SellerSummary(Long id, String shopName) {}
}
