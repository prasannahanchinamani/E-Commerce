package customDesign.E_commerce.dto.mediator;

import java.util.List;

public class SellerInviteRequest {
    private String email;
    private String phone;
    private List<Long> categoryIds;
    public boolean isValidContact() {
        return (email != null && !email.isBlank())
                || (phone != null && !phone.isBlank());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
