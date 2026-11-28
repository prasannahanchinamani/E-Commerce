package customDesign.E_commerce.dto;

import java.math.BigDecimal;

public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    private Long categoryId;
    private Integer estimatedProductionHours;
    private Integer daysToMake;
    private String specifications;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getEstimatedProductionHours() { return estimatedProductionHours; }
    public void setEstimatedProductionHours(Integer hours) { this.estimatedProductionHours = hours; }
    public Integer getDaysToMake() { return daysToMake; }
    public void setDaysToMake(Integer daysToMake) { this.daysToMake = daysToMake; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
}
