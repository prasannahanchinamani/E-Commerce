package customDesign.E_commerce.dto;

public class RefreshTokenRequest {
    @jakarta.validation.constraints.NotBlank(message = "refreshToken is required")
    public String refreshToken;


    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }


}
