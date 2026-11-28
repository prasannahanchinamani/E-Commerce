package customDesign.E_commerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "buyer_order_items")
public class BuyerOrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private BuyerOrder order;
    private Long productId;
    @Column(nullable = false)
    private String productName;
    @Column(length = 1000)
    private String productDescription;
    @Column(length = 2000)
    private String specifications;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    @Column(nullable = false)
    private String sellerEmail;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(nullable = false)
    private Integer quantity;
    private Integer estimatedProductionHours;
    private Integer estimatedProductionDays;
    private Integer actualProductionHours;
    private Instant productionStartedAt;
    private Instant productionCompletedAt;

    public Long getId() { return id; }
    public BuyerOrder getOrder() { return order; }
    public void setOrder(BuyerOrder order) { this.order = order; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getEstimatedProductionHours() { return estimatedProductionHours; }
    public void setEstimatedProductionHours(Integer hours) { this.estimatedProductionHours = hours; }
    public Integer getActualProductionHours() { return actualProductionHours; }
    public void setActualProductionHours(Integer hours) { this.actualProductionHours = hours; }
    public Instant getProductionStartedAt() { return productionStartedAt; }
    public void setProductionStartedAt(Instant at) { this.productionStartedAt = at; }
    public Instant getProductionCompletedAt() { return productionCompletedAt; }
    public void setProductionCompletedAt(Instant at) { this.productionCompletedAt = at; }
    public Integer getEstimatedProductionDays() { return estimatedProductionDays; }
    public void setEstimatedProductionDays(Integer days) { this.estimatedProductionDays = days; }
}
