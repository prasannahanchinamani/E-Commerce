package customDesign.E_commerce.dto;

public class AddressRequest {
    private String fullName;
    @jakarta.validation.constraints.Pattern(regexp = "\\d{10}", message = "phone must contain 10 digits")
    private String phone;
    @jakarta.validation.constraints.NotBlank(message = "line1 is required")
    private String line1;
    private String line2;
    @jakarta.validation.constraints.NotBlank(message = "city is required")
    private String city;
    @jakarta.validation.constraints.NotBlank(message = "state is required")
    private String state;
    @jakarta.validation.constraints.Pattern(regexp = "\\d{6}", message = "pincode must contain 6 digits")
    private String pincode;
    private boolean isDefault;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
