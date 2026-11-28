package customDesign.E_commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {
    private CartDtos() {}
    public record AddCartItemRequest(@NotNull Long productId, @NotNull @Positive Integer quantity) {}
    public record UpdateCartItemRequest(@NotNull @Positive Integer quantity) {}
    public record CheckoutRequest(@NotNull Long addressId) {}
    public record CartItemResponse(Long id, Long productId, String name, String description,
                                   String imageUrl, Long sellerId, String sellerName,
                                   String sellerEmail, BigDecimal unitPrice,
                                   Integer quantity, BigDecimal subtotal) {}
    public record CartResponse(Long id, Long buyerId, List<CartItemResponse> items, BigDecimal total) {}
}
