package customDesign.E_commerce.dto.seller;

import java.util.List;

public class SellerApplyRequest {
    @jakarta.validation.constraints.NotBlank(message = "shopName is required")
    private String shopName;
    private String bio;
    private List<Long> categoryIds;
    private List<Long> portfolioImageIds;
    private String mediatorCode;      // e.g. "MEERA-CRAFTS"
    private String accountName;
    @jakarta.validation.constraints.Pattern(regexp = "\\d{9,18}", message = "accountNumber must contain 9 to 18 digits")
    private String accountNumber;
    private String instagramId;

    public String getInstagramId() { return instagramId; }
    public void setInstagramId(String instagramId) { this.instagramId = instagramId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
    public List<Long> getPortfolioImageIds() { return portfolioImageIds; }
    public void setPortfolioImageIds(List<Long> portfolioImageIds) { this.portfolioImageIds = portfolioImageIds; }
    public String getMediatorCode() { return mediatorCode; }
    public void setMediatorCode(String mediatorCode) { this.mediatorCode = mediatorCode; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}