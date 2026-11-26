package customDesign.E_commerce.dto;

public class RegisterResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String message;
    private Long phone;
    private String status;
    private java.time.Instant createdAt;


    public RegisterResponse(String fullName, String email, String role, String message) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPhone() { return phone; }
    public void setPhone(Long phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
