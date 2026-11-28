package customDesign.E_commerce.controller.admin;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.dto.admin.AdminAuditDtos.*;
import customDesign.E_commerce.dto.admin.AdminUserDtos.*;
import customDesign.E_commerce.entity.Role;
import customDesign.E_commerce.service.admin.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    @GetMapping("/users") public PagedApiResponse<UserResponse> users(@RequestParam(required=false) String role,@RequestParam(required=false) String status,@RequestParam(required=false) String q,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){Role parsed=null;if(role!=null&&!role.isBlank()){try{parsed=Role.valueOf(role.toUpperCase());}catch(IllegalArgumentException ex){throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid role");}}return service.searchUsers(parsed,status,q,page,size);}
    @GetMapping("/users/{id}") public ApiResponse<UserResponse> getUser(@PathVariable Long id){return new ApiResponse<>(true,service.getUser(id));}
    @PutMapping("/users/{id}/status") public ApiResponse<StatusResponse> status(@PathVariable Long id,@Valid @RequestBody UpdateStatusRequest request){return new ApiResponse<>(true,service.updateStatus(id,request));}
    @PostMapping("/users") public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateAdminRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true,service.createAdmin(request)));}
    @PutMapping("/users/{id}/role") public ApiResponse<RoleResponse> role(@PathVariable Long id,@Valid @RequestBody UpdateRoleRequest request){return new ApiResponse<>(true,service.changeRole(id,request));}
    @PostMapping("/mediators") public ResponseEntity<ApiResponse<MediatorResponse>> createMediator(@Valid @RequestBody CreateMediatorRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true,service.createMediator(request)));}
    @GetMapping("/mediators") public PagedApiResponse<MediatorResponse> mediators(@RequestParam(required=false) String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.listMediators(status,page,size);}
    @GetMapping("/mediators/{id}") public ApiResponse<MediatorResponse> mediator(@PathVariable Long id){return new ApiResponse<>(true,service.getMediator(id));}
    @PutMapping("/mediators/{id}/approve") public ApiResponse<StatusResponse> approveMediator(@PathVariable Long id,@RequestBody(required=false) ApproveOrSuspendRequest request){return new ApiResponse<>(true,service.approveMediator(id,request));}
    @PutMapping("/mediators/{id}/suspend") public ApiResponse<StatusResponse> suspendMediator(@PathVariable Long id,@Valid @RequestBody ApproveOrSuspendRequest request){return new ApiResponse<>(true,service.suspendMediator(id,request));}
    @GetMapping("/sellers") public PagedApiResponse<UserResponse> sellers(@RequestParam(required=false) String status,@RequestParam(required=false) Long mediatorId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.listSellers(status,mediatorId,page,size);}
    @PutMapping("/sellers/{id}/status") public ApiResponse<StatusResponse> sellerStatus(@PathVariable Long id,@Valid @RequestBody UpdateStatusRequest request){return new ApiResponse<>(true,service.updateSellerStatus(id,request));}
    @PutMapping("/sellers/{id}/mediator") public ApiResponse<UserResponse> assignSellerMediator(@PathVariable Long id,@Valid @RequestBody AssignSellerMediatorRequest request){return new ApiResponse<>(true,service.assignSellerMediator(id,request));}
    @GetMapping("/audit-logs") public PagedApiResponse<AuditResponse> audit(@RequestParam(required=false) Long actorId,@RequestParam(required=false) String action,@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.auditLogs(actorId,action,from,to,page,size);}
    @GetMapping("/settings") public ApiResponse<java.util.Map<String,Object>> settings(){return new ApiResponse<>(true,service.platformSettings());}
    @PutMapping("/settings") public ApiResponse<SettingResponse> setting(@Valid @RequestBody SettingRequest request){return new ApiResponse<>(true,service.updateSetting(request.key(),request.value()));}
}
