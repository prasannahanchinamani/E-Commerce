package customDesign.E_commerce.dto.seller;

public class SellerProfileUpdateRequest {
    private String shopName;
    private String bio;
    private String story;
    private Long logoId;
    private String instagramId;

    public String getInstagramId() { return instagramId; }
    public void setInstagramId(String instagramId) { this.instagramId = instagramId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }
    public Long getLogoId() { return logoId; }
    public void setLogoId(Long logoId) { this.logoId = logoId; }
}