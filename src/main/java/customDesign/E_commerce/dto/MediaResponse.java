package customDesign.E_commerce.dto;

public class MediaResponse {

    private Long id;
    private String url;
    private String contentType;
    private long size;
    private String purpose;
    private String category;
    private Long orderId;

    public MediaResponse(Long id, String url, String contentType,
                         long size, String purpose, String category, Long orderId) {
        this.id = id;
        this.url = url;
        this.contentType = contentType;
        this.size = size;
        this.purpose = purpose;
        this.category = category;
        this.orderId = orderId;
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public String getPurpose() { return purpose; }
    public String getCategory() { return category; }
    public Long getOrderId() { return orderId; }
}