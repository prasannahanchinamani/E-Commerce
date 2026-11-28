package customDesign.E_commerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private BigDecimal price;

    private String imageUrl;

    private Integer stock;

    private String sellerEmail;

    private boolean active = true;

    private Long designTypeId;
    private java.math.BigDecimal bulkPrice;
    private Integer minBulkQty;
    private Integer daysToMake;
    private Integer estimatedProductionHours;
    @Column(length = 2000)
    private String specifications;

    public Long getDesignTypeId() { return designTypeId; }
    public void setDesignTypeId(Long designTypeId) { this.designTypeId = designTypeId; }
    public java.math.BigDecimal getBulkPrice() { return bulkPrice; }
    public void setBulkPrice(java.math.BigDecimal bulkPrice) { this.bulkPrice = bulkPrice; }
    public Integer getMinBulkQty() { return minBulkQty; }
    public void setMinBulkQty(Integer minBulkQty) { this.minBulkQty = minBulkQty; }
    public Integer getDaysToMake() { return daysToMake; }
    public void setDaysToMake(Integer daysToMake) { this.daysToMake = daysToMake; }
    public Integer getEstimatedProductionHours() { return estimatedProductionHours; }
    public void setEstimatedProductionHours(Integer hours) { this.estimatedProductionHours = hours; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSellerEmail() { return sellerEmail; }

    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

}