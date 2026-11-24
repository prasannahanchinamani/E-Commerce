package customDesign.E_commerce.dto.seller;

import java.util.List;

public class SellerProfileResponse {
    private Long id;
    private String shopName;
    private String bio;
    private List<Long> categoryIds;
    private Long mediatorId;
    private String status;
    private Double rating;


    public SellerProfileResponse(Long id, String shopName, String bio, List<Long> categoryIds, Long mediatorId, String status, Double rating) {
        this.id = id;
        this.shopName = shopName;
        this.bio = bio;
        this.categoryIds = categoryIds;
        this.mediatorId = mediatorId;
        this.status = status;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public String getShopName() { return shopName; }
    public String getBio() { return bio; }
    public List<Long> getCategoryIds() { return categoryIds; }
    public Long getMediatorId() { return mediatorId; }
    public String getStatus() { return status; }
    public Double getRating() { return rating; }
}