package customDesign.E_commerce.service.admin;

import customDesign.E_commerce.dto.PageInfo;
import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.dto.admin.AdminUserDtos.*;
import customDesign.E_commerce.entity.AuditLog;
import customDesign.E_commerce.entity.PlatformSetting;
import customDesign.E_commerce.entity.Role;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.AuditLogRepository;
import customDesign.E_commerce.repository.PlatformSettingRepository;
import customDesign.E_commerce.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final PlatformSettingRepository settings;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository users, AuditLogRepository auditLogs,
                        PlatformSettingRepository settings, PasswordEncoder passwordEncoder) {
        this.users = users; this.auditLogs = auditLogs; this.settings = settings; this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly=true)
    public PagedApiResponse<UserResponse> searchUsers(Role role, String status, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1),100), Sort.by(Sort.Direction.DESC,"createdAt"));
        Page<User> result = users.searchAdmin(role,status,q,pageable);
        return new PagedApiResponse<>(true,result.map(this::toUser).getContent(),new PageInfo(result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages()));
    }

    @Transactional(readOnly=true) public UserResponse getUser(Long id) { return toUser(findUser(id)); }

    @Transactional
    public StatusResponse updateStatus(Long id, UpdateStatusRequest request) {
        if(request==null||request.status()==null||request.status().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"status is required");
        User user=findUser(id); String status=request.status().toUpperCase();
        if(!List.of("ACTIVE","BLOCKED","PENDING","APPROVED","REJECTED","SUSPENDED").contains(status)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid status");
        user.setStatus(status); users.save(user); audit("USER_STATUS_CHANGED","user:"+id); return new StatusResponse(id,status);
    }

    @Transactional
    public UserResponse createAdmin(CreateAdminRequest request) {
        if(request==null||request.name()==null||request.name().isBlank()||request.email()==null||request.email().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"name and email are required");
        if(!"ADMIN".equalsIgnoreCase(request.role())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"role must be ADMIN");
        if(users.findByEmail(request.email().toLowerCase()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");
        User user=new User(); user.setName(request.name().trim()); user.setEmail(request.email().trim().toLowerCase()); user.setRole(Role.ADMIN); user.setStatus("ACTIVE"); user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); users.save(user);
        audit("ADMIN_CREATED","user:"+user.getId()); return toUser(user);
    }

    @Transactional
    public RoleResponse changeRole(Long id, UpdateRoleRequest request) {
        if(request==null||request.role()==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"role is required");
        Role role; try{role=Role.valueOf(request.role().toUpperCase());}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid role");}
        Authentication auth=SecurityContextHolder.getContext().getAuthentication(); User acting=auth==null?null:users.findByEmail(auth.getName()).orElse(null);
        if(acting!=null&&acting.getId().equals(id)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You cannot change your own role");
        User user=findUser(id); user.setRole(role); users.save(user); audit("USER_ROLE_CHANGED","user:"+id); return new RoleResponse(id,role.name());
    }

    @Transactional
    public MediatorResponse createMediator(CreateMediatorRequest request) {
        if(request==null||request.name()==null||request.name().isBlank()||request.email()==null||request.email().isBlank()||request.shopName()==null||request.shopName().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"name, email and shopName are required");
        if(users.findByEmail(request.email().toLowerCase()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");
        User m=new User(); m.setName(request.name().trim()); m.setEmail(request.email().trim().toLowerCase()); m.setRole(Role.MEDIATOR); m.setStatus("APPROVED"); m.setShopName(request.shopName().trim()); m.setMediatorCode(slug(request.shopName()));
        if(request.phone()!=null&&!request.phone().isBlank()){try{m.setNumber(Long.parseLong(request.phone().replaceAll("\\D","")));}catch(NumberFormatException ignored){}}
        m.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); users.save(m); audit("MEDIATOR_CREATED","user:"+m.getId()); return toMediator(m);
    }

    @Transactional(readOnly=true)
    public PagedApiResponse<MediatorResponse> listMediators(String status,int page,int size){
        Pageable pageable=PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100),Sort.by(Sort.Direction.DESC,"createdAt"));
        Page<User> result=(status==null||status.isBlank())?users.findByRole(Role.MEDIATOR,pageable):users.findByRoleAndStatus(Role.MEDIATOR,status.toUpperCase(),pageable);
        return new PagedApiResponse<>(true,result.map(this::toMediator).getContent(),new PageInfo(result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages()));
    }

    @Transactional(readOnly=true) public MediatorResponse getMediator(Long id){return toMediator(findRole(id,Role.MEDIATOR));}

    @Transactional public StatusResponse approveMediator(Long id,ApproveOrSuspendRequest request){User m=findRole(id,Role.MEDIATOR);m.setStatus("APPROVED");users.save(m);audit("MEDIATOR_APPROVED","user:"+id);return new StatusResponse(id,"APPROVED");}

    @Transactional public StatusResponse suspendMediator(Long id,ApproveOrSuspendRequest request){if(request==null||request.reason()==null||request.reason().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"reason is required");User m=findRole(id,Role.MEDIATOR);m.setStatus("SUSPENDED");users.save(m);audit("MEDIATOR_SUSPENDED","user:"+id);return new StatusResponse(id,"SUSPENDED");}

    @Transactional(readOnly=true)
    public PagedApiResponse<UserResponse> listSellers(String status,Long mediatorId,int page,int size){
        Pageable pageable=PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100),Sort.by(Sort.Direction.DESC,"createdAt"));Page<User> result;
        if(mediatorId!=null)result=(status==null||status.isBlank())?users.findByMediatorIdAndRole(mediatorId,Role.SELLER,pageable):users.findByMediatorIdAndRoleAndStatus(mediatorId,Role.SELLER,status.toUpperCase(),pageable);
        else result=(status==null||status.isBlank())?users.findByRole(Role.SELLER,pageable):users.findByRoleAndStatus(Role.SELLER,status.toUpperCase(),pageable);
        return new PagedApiResponse<>(true,result.map(this::toUser).getContent(),new PageInfo(result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages()));
    }

    @Transactional public StatusResponse updateSellerStatus(Long id,UpdateStatusRequest request){if(request==null||request.status()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"status is required");User s=findRole(id,Role.SELLER);String status=request.status().toUpperCase();if(!List.of("APPROVED","REJECTED","SUSPENDED").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid seller status");s.setStatus(status);users.save(s);audit("SELLER_STATUS_CHANGED","user:"+id);return new StatusResponse(id,status);}

    @Transactional
    public UserResponse assignSellerMediator(Long sellerId, AssignSellerMediatorRequest request) {
        if (request == null || request.mediatorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mediatorId is required");
        }
        User seller = findRole(sellerId, Role.SELLER);
        User mediator = findRole(request.mediatorId(), Role.MEDIATOR);
        if (!"APPROVED".equals(mediator.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mediator must be approved before assignment");
        }
        seller.setMediatorId(mediator.getId());
        users.save(seller);
        audit("SELLER_MEDIATOR_ASSIGNED", "seller:" + sellerId + ",mediator:" + mediator.getId());
        return toUser(seller);
    }

    @Transactional(readOnly=true)
    public PagedApiResponse<customDesign.E_commerce.dto.admin.AdminAuditDtos.AuditResponse> auditLogs(Long actorId,String action,Instant from,Instant to,int page,int size){
        Pageable pageable=PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100),Sort.by(Sort.Direction.DESC,"at"));Page<AuditLog> result=auditLogs.search(actorId,action,from,to,pageable);
        return new PagedApiResponse<>(true,result.map(a->new customDesign.E_commerce.dto.admin.AdminAuditDtos.AuditResponse(a.getId(),a.getActorId(),a.getActorRole(),a.getAction(),a.getTarget(),a.getAt(),a.getIp())).getContent(),new PageInfo(result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages()));
    }

    @Transactional(readOnly=true)
    public java.util.Map<String,Object> platformSettings(){java.util.Map<String,String> v=settings.findAll().stream().collect(java.util.stream.Collectors.toMap(PlatformSetting::getKey,PlatformSetting::getValue));return java.util.Map.of("supportEmail",v.getOrDefault("supportEmail","help@example.com"),"maintenanceMode",v.getOrDefault("maintenanceMode","false").equals("true"),"defaultPageSize",Integer.parseInt(v.getOrDefault("defaultPageSize","20")));}

    @Transactional public customDesign.E_commerce.dto.admin.AdminAuditDtos.SettingResponse updateSetting(String key,String value){if(key==null||key.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"key is required");PlatformSetting s=settings.findById(key).orElse(new PlatformSetting());s.setKey(key);s.setValue(value);settings.save(s);audit("PLATFORM_SETTING_CHANGED","setting:"+key);return new customDesign.E_commerce.dto.admin.AdminAuditDtos.SettingResponse(key,value);}

    private User findUser(Long id){return users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));}
    private User findRole(Long id,Role role){return users.findById(id).filter(u->u.getRole()==role).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,role.name().toLowerCase()+" not found"));}
    private UserResponse toUser(User u){String phone=u.getNumber()==null?null:u.getNumber().toString();Instant created=u.getCreatedAt()==null?null:u.getCreatedAt().toInstant(ZoneOffset.UTC);return new UserResponse(u.getId(),u.getName(),u.getEmail(),phone,u.getRole()==null?null:u.getRole().name(),u.getStatus(),created);}
    private MediatorResponse toMediator(User u){long count=users.findByMediatorIdAndRole(u.getId(),Role.SELLER).size();return new MediatorResponse(u.getId(),u.getName(),u.getShopName(),u.getMediatorCode(),u.getStatus()==null?"APPROVED":u.getStatus(),count);}
    private String slug(String value){return value.toLowerCase().replaceAll("[^a-z0-9]+","-").replaceAll("^-|-$","");}
    private void audit(String action,String target){Authentication auth=SecurityContextHolder.getContext().getAuthentication();User actor=auth==null?null:users.findByEmail(auth.getName()).orElse(null);AuditLog l=new AuditLog();l.setActorId(actor==null?null:actor.getId());l.setActorRole(actor==null||actor.getRole()==null?null:actor.getRole().name());l.setAction(action);l.setTarget(target);auditLogs.save(l);}
}
