package customDesign.E_commerce.entity.seller;

import customDesign.E_commerce.entity.InviteStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seller_invites")
public class SellerInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mediatorId;

    private String email;
    private String phone;

    @Column(nullable = false, unique = true)
    private String token;

    @ElementCollection
    @CollectionTable(name = "seller_invite_categories",
            joinColumns = @JoinColumn(name = "invite_id"))
    @Column(name = "category_id")
    private List<Long> categoryIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.INVITED;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMediatorId() { return mediatorId; }
    public void setMediatorId(Long mediatorId) { this.mediatorId = mediatorId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
    public InviteStatus getStatus() { return status; }
    public void setStatus(InviteStatus status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}