package customDesign.E_commerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateOrderRequest {
    @NotNull(message = "listingId is required")
    private Long listingId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private Integer quantity;

    @NotNull(message = "addressId is required")
    private Long addressId;

    private String note;
    private String couponCode;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
}
