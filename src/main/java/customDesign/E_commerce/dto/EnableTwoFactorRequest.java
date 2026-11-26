package customDesign.E_commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EnableTwoFactorRequest {
    @NotBlank(message = "method is required")
    @Pattern(regexp = "TOTP", message = "method must be TOTP")
    private String method;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
