package customDesign.E_commerce.service;

import customDesign.E_commerce.Exceptions.BadRequestException;
import customDesign.E_commerce.Exceptions.ResourceNotFoundException;
import customDesign.E_commerce.Exceptions.UserAlreadyFound;
import customDesign.E_commerce.Exceptions.UserNotFoundException;
import customDesign.E_commerce.dto.*;
import customDesign.E_commerce.entity.PasswordResetToken;
import customDesign.E_commerce.entity.Role;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.PasswordResetTokenRepository;
import customDesign.E_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class authServiceForLgRg {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    TokenService tokenService;

    @Autowired
    PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    MailService mailService;

    @Value("${app.frontend-url}")
    String frontendUrl;

    @Value("${app.mail.from}")
    private String from;

    public authServiceForLgRg(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);

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

    public LoginResponse login(LoginRequest request) throws BadRequestException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Invalid email or password"
                        )
                );
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadRequestException("Invalid email or password");
        }
        LoginResponse response = new LoginResponse();

        response.setAccessToken(
                tokenService.generateToken(user)
        );

        response.setRefreshToken(
                tokenService.refreshToken(user)
        );
        response.setTokenType("Bearer");
        response.setExpiresIn(3600);
        response.setUser(user);

        return response;
    }
    public LoginResponse refreshToken(String token) {
        if (token == null || !tokenService.isRefreshToken(token)) {
            throw new BadRequestException("A valid refresh token is required");
        }
        String email = tokenService.ExtractEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        LoginResponse response = new LoginResponse();
        response.setAccessToken(tokenService.generateToken(user));
        response.setRefreshToken(tokenService.refreshToken(user));
        response.setTokenType("Bearer");
        response.setExpiresIn(3600);
        return response;
    }
    @Transactional
    public String forgetPassword(ForgetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException( "User not found with email: " + request.getEmail() ));
        resetTokenRepository.deleteByUser(user);
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(sha256(rawToken));
        resetToken.setExpiresAt(Instant.now().plus(TOKEN_TTL));
        resetTokenRepository.saveAndFlush(resetToken);
        String link = frontendUrl + "/reset-password?token=" + rawToken;
        mailService.sendEmail( from, user.getEmail(),"Password Reset Request", link);
        return "Password reset link sent successfully to " + user.getEmail();
    }
    @Transactional
    public String resetPassword(ResetPasswordRequest request) throws BadRequestException {
        if (request.getToken() == null || request.getNewPassword() == null) {
            throw new BadRequestException("Token and new password must not be null");
        }

        PasswordResetToken t = resetTokenRepository.findByTokenHash(sha256(request.getToken()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired link"));

        if (t.getExpiresAt().isBefore(Instant.now())) {
            resetTokenRepository.delete(t);
            throw new BadRequestException("Invalid or expired link");
        }

        User user = t.getUser();
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        resetTokenRepository.delete(t);   // link can be used only once
        return "Password reset successfully";
    }

    private static String sha256(String value) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

}