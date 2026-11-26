package customDesign.E_commerce.dto.mediator;

import java.time.Instant;

public class SellerInviteResponse {
    private Long id;
    private String email;
    private String status;
    private Instant expiresAt;

    public SellerInviteResponse(Long id, String email, String status, Instant expiresAt) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
