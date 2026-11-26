package customDesign.E_commerce.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_actor", columnList = "actorId"),
    @Index(name = "idx_audit_action", columnList = "action")
})
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long actorId;
    private String actorRole;
    private String action;
    private String target;
    private String ip;
    @Column(nullable=false)
    private Instant at;

    @PrePersist
    public void onCreate() { if (at == null) at = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Instant getAt() { return at; }
    public void setAt(Instant at) { this.at = at; }
}
