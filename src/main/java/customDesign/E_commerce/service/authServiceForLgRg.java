package customDesign.E_commerce.service;

import customDesign.E_commerce.Exceptions.ResourceNotFoundException;
import customDesign.E_commerce.Exceptions.UserAlreadyFound;
import customDesign.E_commerce.Exceptions.UserNotFoundException;
import customDesign.E_commerce.dto.*;
import customDesign.E_commerce.entity.Role;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class authServiceForLgRg {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public authServiceForLgRg(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public RegisterResponse registerBuyer(RegisterRequest request) {
        User user = new User();
        String message = "User registered successfully";
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            message = "User with email " + request.getEmail() + " already exists";
            throw new UserAlreadyFound(message);
        }
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(user.getRole());
        user.setRole(Role.valueOf(request.getRole().name()));
        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getName(), savedUser.getEmail(), savedUser.getRole().name(), message);
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        LoginResponse response = new LoginResponse();
        if (user.isPresent()) {
            if ((request.getEmail().equalsIgnoreCase(user.get().getEmail())) && passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
                response.setAccessToken("accessToken");
                response.setRefreshToken("refreshToken");
                response.setExpiresIn(3600);
                response.setUser(user.get());
            }
        }else{
            throw new UserNotFoundException("User is not registered with this email: " + request.getEmail());
        }
        return response;
    }
    public LoginResponse refreshToken(String refreshTokenRequest) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken("accessToken");
        response.setRefreshToken(Math.random()*1000+" "+refreshTokenRequest);
        response.setExpiresIn(3600);
        return response;
    }
    public String forgetPassword(ForgetPasswordRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            return "Password reset link sent to email: " + request.getEmail();
        }else{
            throw new UserNotFoundException("User is not registered with this email: " + request.getEmail());
        }

    }
    public String resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String newPassword = request.getNewPassword();
        if (email == null || newPassword == null) {
            throw new ResourceNotFoundException("Email and new password must not be null");
        }
        if (!userRepository.findByEmail(email).isPresent()) {
            throw new UserNotFoundException("User is not registered with this email: " + email);
        }
        String oldPassword = userRepository.findByEmail(email).get().getPassword();
        if (oldPassword.equals(newPassword)) {
            throw new ResourceNotFoundException("New password must be different from the old password");
        }
        User user = userRepository.findByEmail(email).get();
        user.setPassword(newPassword);
        userRepository.save(user);
        return "Password reset successfully for email: " + email;
    }

}