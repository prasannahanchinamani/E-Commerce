package customDesign.E_commerce.controller;

import customDesign.E_commerce.dto.ApiResponse;
import customDesign.E_commerce.dto.ChangePasswordRequest;
import customDesign.E_commerce.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    @Autowired
    private UserService userService;

    @PutMapping("/changepassword")
    public ResponseEntity<ApiResponse<String>> changePassword(
           @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String msg = userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, msg));
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully"));
    }
}
