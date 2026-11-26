package customDesign.E_commerce.dto.mediator;

public class SellerActionRequest {
    private String note;
    private String reason;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}