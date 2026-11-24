package customDesign.E_commerce.controller.seller;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.dto.PortfolioRequest;
import customDesign.E_commerce.dto.PortfolioResponse;
import customDesign.E_commerce.dto.seller.SellerApplyRequest;
import customDesign.E_commerce.dto.seller.SellerProfileResponse;
import customDesign.E_commerce.dto.seller.SellerProfileUpdateRequest;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.service.seller.SellerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<User>> apply(
            @RequestBody SellerApplyRequest request,
            Authentication authentication) {
        User user = sellerService.applyAsSeller(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, user));
    }
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getProfile(Authentication authentication) {
        SellerProfileResponse profile = sellerService.getProfile(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, profile));
    }
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> updateProfile(
            @RequestBody SellerProfileUpdateRequest request,
            Authentication authentication) {
        SellerProfileResponse profile = sellerService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, profile));
    }
    @PostMapping("/portfolio")
    public ResponseEntity<ApiResponse<PortfolioResponse>> addPortfolio(
            @RequestBody PortfolioRequest request,
            Authentication authentication) {
        PortfolioResponse response = sellerService.addPortfolio(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, response));
    }
}