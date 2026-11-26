package customDesign.E_commerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "buyer_orders", indexes = {
    @Index(name = "idx_order_buyer", columnList = "buyerEmail"),
    @Index(name = "idx_order_seller", columnList = "sellerEmail"),
    @Index(name = "idx_order_status", columnList = "status")
})
public class BuyerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String buyerEmail;

    private String sellerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal commissionRate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal sellerPayout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.AWAITING_PAYMENT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private String note;
    private String trackingInfo;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }
    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }
    public BigDecimal getSellerPayout() { return sellerPayout; }
    public void setSellerPayout(BigDecimal sellerPayout) { this.sellerPayout = sellerPayout; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getTrackingInfo() { return trackingInfo; }
    public void setTrackingInfo(String trackingInfo) { this.trackingInfo = trackingInfo; }
}
