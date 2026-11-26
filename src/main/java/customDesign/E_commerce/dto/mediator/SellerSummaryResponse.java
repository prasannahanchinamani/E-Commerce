package customDesign.E_commerce.dto.mediator;

import java.util.List;

public class SellerSummaryResponse {
    Long id; String shopName; String bio;

    public SellerSummaryResponse(Long id, String shopName, String bio, List<Long> categoryIds, Long mediatorId, String status, Double rating) {
        this.id = id;
        this.shopName = shopName;
        this.bio = bio;
        this.categoryIds = categoryIds;
        this.mediatorId = mediatorId;
        this.status = status;
        this.rating = rating;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Long getMediatorId() {
        return mediatorId;
    }

    public void setMediatorId(Long mediatorId) {
        this.mediatorId = mediatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    List<Long> categoryIds; Long mediatorId; String status; Double rating;

}
