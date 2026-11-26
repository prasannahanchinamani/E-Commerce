package customDesign.E_commerce.controller;

import customDesign.E_commerce.dto.AccountResponse;
import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.dto.ChangePasswordRequest;
import customDesign.E_commerce.dto.UpdateAccountRequest;
import customDesign.E_commerce.service.UserService;
import customDesign.E_commerce.service.authServiceForLgRg;
import customDesign.E_commerce.dto.RefreshTokenRequest;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    private authServiceForLgRg authService;

    @PutMapping("/changepassword")
    public ResponseEntity<ApiResponse<String>> changePassword(
           @jakarta.validation.Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String msg = userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, msg));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@jakarta.validation.Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/2fa/enable")
    public ApiResponse<customDesign.E_commerce.dto.TwoFactorSetupResponse> enableTwoFactor(
            @jakarta.validation.Valid @RequestBody customDesign.E_commerce.dto.EnableTwoFactorRequest request,
            Authentication authentication) {
        return new ApiResponse<>(true, userService.enableTwoFactor(authentication.getName(), request));
    }

    @PostMapping("/2fa/verify")
    public ApiResponse<customDesign.E_commerce.dto.TwoFactorVerifyResponse> verifyTwoFactor(
            @jakarta.validation.Valid @RequestBody customDesign.E_commerce.dto.VerifyTwoFactorRequest request,
            Authentication authentication) {
        return new ApiResponse<>(true, userService.verifyTwoFactor(authentication.getName(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> me(
            Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(true,
                userService.getMyAccount(authentication.getName())));
    }
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>>updateMe(@jakarta.validation.Valid @RequestBody UpdateAccountRequest updateAccountRequest,Authentication authentication){
        return ResponseEntity.ok(new ApiResponse<>(true,userService.updateMyAccount(authentication.getName(),updateAccountRequest)));
    }
}
