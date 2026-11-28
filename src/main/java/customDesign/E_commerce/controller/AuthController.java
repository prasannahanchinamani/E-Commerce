package customDesign.E_commerce.controller;

import ch.qos.logback.core.status.Status;
import customDesign.E_commerce.Exceptions.BadRequestException;
import customDesign.E_commerce.dto.*;
import customDesign.E_commerce.service.authServiceForLgRg;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    authServiceForLgRg registerLoginService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>>register(@Valid @RequestBody RegisterRequest registerRequest){
        RegisterResponse registerResponse=registerLoginService.registerBuyer(registerRequest);
//        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, registerResponse));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>>login(@Valid @RequestBody LoginRequest loginRequest) throws BadRequestException {
       LoginResponse loginResponse=registerLoginService.login(loginRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, loginResponse));
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>>refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokem){
        LoginResponse loginResponse=registerLoginService.refreshToken(refreshTokem.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse<>(true, loginResponse));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        registerLoginService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/activity")
    public ResponseEntity<Void> activity(Authentication authentication) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>>forgetPassword(@Valid @RequestBody ForgetPasswordRequest forgetPasswordRequest){
        registerLoginService.forgetPassword(forgetPasswordRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse<>(true, "If the email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")

    public ResponseEntity<ApiResponse<String>>resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) throws BadRequestException {
        String response=registerLoginService.resetPassword(resetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, response));
    }



}
