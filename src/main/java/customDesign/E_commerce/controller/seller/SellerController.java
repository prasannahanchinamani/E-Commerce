package customDesign.E_commerce.controller.seller;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.dto.PortfolioRequest;
import customDesign.E_commerce.dto.ProductRequest;
import customDesign.E_commerce.dto.PortfolioResponse;
import customDesign.E_commerce.dto.seller.SellerApplyRequest;
import customDesign.E_commerce.dto.seller.SellerProfileResponse;
import customDesign.E_commerce.dto.seller.SellerProfileUpdateRequest;
import customDesign.E_commerce.entity.Product;
import customDesign.E_commerce.service.seller.SellerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<ApiResponse<String>> acceptInvitation(
            @PathVariable String token, Authentication authentication) {
        String message = sellerService.acceptInvitation(authentication.getName(), token);
        return ResponseEntity.ok(new ApiResponse<>(true, message));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('BUYER','SELLER')")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> apply(
            @jakarta.validation.Valid @RequestBody SellerApplyRequest request,
            Authentication authentication) {
        sellerService.applyAsSeller(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true,
                sellerService.getProfile(authentication.getName())));
    }
    @GetMapping("/profile")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getProfile(Authentication authentication) {
        SellerProfileResponse profile = sellerService.getProfile(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, profile));
    }
    @PutMapping("/profile")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerProfileResponse>> updateProfile(
            @jakarta.validation.Valid @RequestBody SellerProfileUpdateRequest request,
            Authentication authentication) {
        SellerProfileResponse profile = sellerService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, profile));
    }
    @GetMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<java.util.List<Product>>> getProducts(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(true, sellerService.getProducts(authentication.getName())));
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(true, sellerService.getProduct(authentication.getName(), id)));
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody ProductRequest request,
                                                              Authentication authentication) {
        Product product = sellerService.createProduct(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, product));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long id,
                                                               @RequestBody ProductRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(true,
                sellerService.updateProduct(authentication.getName(), id, request)));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id,
                                                              Authentication authentication) {
        sellerService.deleteProduct(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product deleted successfully"));
    }

    @GetMapping("/portfolio")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<java.util.List<PortfolioResponse>>> getPortfolio(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(true, sellerService.getPortfolio(authentication.getName())));
    }

    @DeleteMapping("/portfolio/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<String>> deletePortfolio(
            @PathVariable Long id, Authentication authentication) {
        sellerService.deletePortfolio(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Portfolio item deleted successfully"));
    }

    @PostMapping("/portfolio")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PortfolioResponse>> addPortfolio(
            @jakarta.validation.Valid @RequestBody PortfolioRequest request,
            Authentication authentication) {
        PortfolioResponse response = sellerService.addPortfolio(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, response));
    }
}