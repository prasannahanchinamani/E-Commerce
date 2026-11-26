package customDesign.E_commerce.service.mediator;

import customDesign.E_commerce.dto.PageInfo;
import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.dto.mediator.*;
import customDesign.E_commerce.entity.*;
import customDesign.E_commerce.entity.seller.SellerInvite;
import customDesign.E_commerce.repository.MediatorSeller;
import customDesign.E_commerce.repository.SellerInviteRepository;
import customDesign.E_commerce.repository.UserRepository;
import org.springframework.data.domain.*;
import customDesign.E_commerce.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class MediatorSellerServiceImpl implements MediatorSeller {

    private static final int INVITE_VALIDITY_MONTHS = 6;

    private final SellerInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String mailFrom;

    public MediatorSellerServiceImpl(SellerInviteRepository inviteRepository,
                                     UserRepository userRepository,
                                     MailService mailService) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    // ---------- API-105 invite ----------
    @Override
    @Transactional
    public SellerInviteResponse inviteSeller(SellerInviteRequest request) {
        if (request == null || !request.isValidContact())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either email or phone is required");

        User mediator = getCurrentMediator();
        String email = normalize(request.getEmail());
        String phone = normalize(request.getPhone());

        if (email != null) {
            email = email.toLowerCase();
            if (userRepository.findByEmail(email).isPresent())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A user with this email already exists");
            if (inviteRepository.existsByMediatorIdAndEmailAndStatus(mediator.getId(), email, InviteStatus.INVITED))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An invite already exists for this email");
        } else if (inviteRepository.existsByMediatorIdAndPhoneAndStatus(mediator.getId(), phone, InviteStatus.INVITED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An invite already exists for this phone");
        }

        SellerInvite invite = new SellerInvite();
        invite.setMediatorId(mediator.getId());
        invite.setEmail(email);
        invite.setPhone(phone);
        invite.setCategoryIds(request.getCategoryIds() == null
                ? new ArrayList<>() : new ArrayList<>(request.getCategoryIds()));
        invite.setToken(UUID.randomUUID().toString());
        invite.setStatus(InviteStatus.INVITED);
        invite.setExpiresAt(ZonedDateTime.now(ZoneOffset.UTC).plusMonths(INVITE_VALIDITY_MONTHS).toInstant());

        SellerInvite saved = inviteRepository.save(invite);

        if (saved.getEmail() != null) {
            String link = frontendUrl + "/accept-seller-invite?token=" + saved.getToken();
            mailService.sendInvitation(mailFrom, saved.getEmail(),
                    "Seller invitation", link);
        }

        return new SellerInviteResponse(saved.getId(), saved.getEmail(),
                saved.getStatus().name(), saved.getExpiresAt());
    }

    // ---------- API-106 list ----------
    @Override
    @Transactional(readOnly = true)
    public PagedApiResponse<SellerSummaryResponse> listSellers(String status, int page, int size) {
        User mediator = getCurrentMediator();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> result = (status == null || status.isBlank())
                ? userRepository.findByMediatorIdAndRole(mediator.getId(), Role.SELLER, pageable)
                : userRepository.findByMediatorIdAndRoleAndStatus(
                mediator.getId(), Role.SELLER, status.toUpperCase(), pageable);

        return new PagedApiResponse<>(true,
                result.getContent().stream().map(this::toSummary).toList(),
                new PageInfo(result.getNumber(), result.getSize(),
                        result.getTotalElements(), result.getTotalPages()));
    }

    // ---------- API-107 view ----------
    @Override
    @Transactional(readOnly = true)
    public SellerSummaryResponse getSeller(Long id) {
        return toSummary(findOwnedSeller(id));
    }

    // ---------- API-108 approve ----------
    @Override
    @Transactional
    public SellerStatusResponse approveSeller(Long id, SellerActionRequest req) {
        User seller = findOwnedSeller(id);
        requireStatus(seller, "PENDING");
        seller.setStatus("APPROVED");
        return new SellerStatusResponse(userRepository.save(seller).getId(), "APPROVED");
    }

    // ---------- API-109 reject ----------
    @Override
    @Transactional
    public SellerStatusResponse rejectSeller(Long id, SellerActionRequest req) {
        requireReason(req);
        User seller = findOwnedSeller(id);
        requireStatus(seller, "PENDING");
        seller.setStatus("REJECTED");
        return new SellerStatusResponse(userRepository.save(seller).getId(), "REJECTED");
    }

    // ---------- API-110 suspend ----------
    @Override
    @Transactional
    public SellerStatusResponse suspendSeller(Long id, SellerActionRequest req) {
        requireReason(req);
        User seller = findOwnedSeller(id);
        requireStatus(seller, "APPROVED");
        seller.setStatus("SUSPENDED");
        return new SellerStatusResponse(userRepository.save(seller).getId(), "SUSPENDED");
    }

    // ---------- helpers ----------
    private User getCurrentMediator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mediator not found"));
    }

    private User findOwnedSeller(Long id) {
        User mediator = getCurrentMediator();
        User seller = userRepository.findById(id)
                .filter(u -> u.getRole() == Role.SELLER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));
        if (!mediator.getId().equals(seller.getMediatorId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your seller");
        return seller;
    }

    private void requireStatus(User seller, String expected) {
        if (!expected.equals(seller.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seller must be " + expected + " but is " + seller.getStatus());
    }

    private void requireReason(SellerActionRequest req) {
        if (req == null || req.getReason() == null || req.getReason().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason is required");
    }

    private SellerSummaryResponse toSummary(User u) {
        List<Long> cats = (u.getCategoryIds() == null || u.getCategoryIds().isBlank())
                ? List.of()
                : Arrays.stream(u.getCategoryIds().split(","))
                .map(String::trim).map(Long::valueOf).toList();
        return new SellerSummaryResponse(u.getId(), u.getShopName(), u.getBio(),
                cats, u.getMediatorId(), u.getStatus(), null);
    }

    private String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}