package customDesign.E_commerce.dto;

import customDesign.E_commerce.entity.Role;

public class AccountResponse {
    private Long id;
    private String name;
    private String email;
    private Long phone;
    private Role role;
    private java.util.List<String> roles;
    private boolean twoFactorEnabled;
    private String photoUrl;

    public AccountResponse(Long id, String name, String email, Long phone, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.roles = role == null ? java.util.List.of() : java.util.List.of(role.name());
        this.twoFactorEnabled = false;
        this.photoUrl = null;
    }

    public java.util.List<String> getRoles() { return roles; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public String getPhotoUrl() { return photoUrl; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    public void setRoles(java.util.List<String> roles) { this.roles = roles; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
