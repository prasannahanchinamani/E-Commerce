package customDesign.E_commerce.dto;

public class UpdateAccountRequest {
    @jakarta.validation.constraints.NotBlank(message = "name is required")
    @jakarta.validation.constraints.Size(max = 100, message = "name must be at most 100 characters")
    private String name;
    private String phone;

    public UpdateAccountRequest(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
