package customDesign.E_commerce.dto;

import java.util.List;

public class PortfolioResponse {
    private Long id;
    private String title;
    private List<String> imageUrls;
    private Long categoryId;

    public PortfolioResponse(Long id, String title, List<String> imageUrls, Long categoryId) {
        this.id = id;
        this.title = title;
        this.imageUrls = imageUrls;
        this.categoryId = categoryId;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public List<String> getImageUrls() { return imageUrls; }
    public Long getCategoryId() { return categoryId; }
}