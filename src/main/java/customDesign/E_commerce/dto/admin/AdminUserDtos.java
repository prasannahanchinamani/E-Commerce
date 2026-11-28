package customDesign.E_commerce.dto.admin;

public final class AdminUserDtos {
    private AdminUserDtos() {}

    public record UpdateStatusRequest(String status, String reason) {}
    public record UpdateRoleRequest(String role) {}
    public record CreateAdminRequest(String name, String email, String role) {}
    public record CreateMediatorRequest(String name, String email, String phone, String shopName) {}
    public record AssignSellerMediatorRequest(Long mediatorId) {}
    public record ApproveOrSuspendRequest(String note, String reason) {}

    public record UserResponse(Long id, String name, String email, String phone, String role,
                               String status, java.time.Instant createdAt) {}
    public record StatusResponse(Long id, String status) {}
    public record RoleResponse(Long id, String role) {}
    public record MediatorResponse(Long id, String name, String shopName, String shopSlug,
                                   String status, long sellerCount) {}
}
