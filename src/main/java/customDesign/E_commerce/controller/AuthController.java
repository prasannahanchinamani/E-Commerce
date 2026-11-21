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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    authServiceForLgRg registerLoginService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>>register(@RequestBody RegisterRequest registerRequest){
        RegisterResponse registerResponse=registerLoginService.registerBuyer(registerRequest);
//        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, registerResponse));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>>login(@RequestBody LoginRequest loginRequest) throws BadRequestException {
       LoginResponse loginResponse=registerLoginService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, loginResponse));
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>>refreshToken(@RequestBody RefreshTokenRequest refreshTokem){
        LoginResponse loginResponse=registerLoginService.refreshToken(refreshTokem.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, loginResponse));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>>forgetPassword(@RequestBody ForgetPasswordRequest forgetPasswordRequest){
        String response=registerLoginService.forgetPassword(forgetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Password reset link sent to email: " + forgetPasswordRequest.getEmail()));
    }

    @PostMapping("/reset-password")

    public ResponseEntity<ApiResponse<String>>resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) throws BadRequestException {
        String response=registerLoginService.resetPassword(resetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, response));
    }



}
