package customDesign.E_commerce.dto.admin;

public final class AdminAuditDtos {
    private AdminAuditDtos() {}
    public record AuditResponse(Long id, Long actorId, String actorRole, String action,
                                String target, java.time.Instant at, String ip) {}
    public record SettingRequest(String key, String value) {}
    public record SettingResponse(String key, String value) {}
}
