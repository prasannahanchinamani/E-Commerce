package customDesign.E_commerce.controller.mediator;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.dto.mediator.*;
import customDesign.E_commerce.repository.MediatorSeller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mediator/sellers")
@PreAuthorize("hasRole('MEDIATOR')")
public class MediatorSellerController {

    private final MediatorSeller service;

    public MediatorSellerController(MediatorSeller service) {
        this.service = service;
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<SellerInviteResponse>> invite(@RequestBody SellerInviteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, service.inviteSeller(request)));
    }

    @GetMapping
    public PagedApiResponse<SellerSummaryResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {
        return service.listSellers(status, page, size);
    }

    @GetMapping("/{id}")
    public ApiResponse<SellerSummaryResponse> get(@PathVariable Long id) {
        return new ApiResponse<>(true, service.getSeller(id));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<SellerStatusResponse> approve(@PathVariable Long id,
                                                     @RequestBody(required = false) SellerActionRequest req) {
        return new ApiResponse<>(true, service.approveSeller(id, req));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<SellerStatusResponse> reject(@PathVariable Long id,
                                                    @RequestBody SellerActionRequest req) {
        return new ApiResponse<>(true, service.rejectSeller(id, req));
    }

    @PutMapping("/{id}/suspend")
    public ApiResponse<SellerStatusResponse> suspend(@PathVariable Long id,
                                                     @RequestBody SellerActionRequest req) {
        return new ApiResponse<>(true, service.suspendSeller(id, req));
    }
}