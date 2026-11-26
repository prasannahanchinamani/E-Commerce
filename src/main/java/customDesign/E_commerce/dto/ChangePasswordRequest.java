package customDesign.E_commerce.dto;

public class ChangePasswordRequest {

    @jakarta.validation.constraints.NotBlank(message = "currentPassword is required")
    private String currentPassword;
    @jakarta.validation.constraints.NotBlank(message = "newPassword is required")
    @jakarta.validation.constraints.Size(min = 8, message = "newPassword must be at least 8 characters")
    private String newPassword;

    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    private String confirmPassword;

}