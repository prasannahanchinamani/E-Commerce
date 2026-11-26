package customDesign.E_commerce.service;

import customDesign.E_commerce.Exceptions.UserNotFoundException;
import customDesign.E_commerce.dto.AccountResponse;
import customDesign.E_commerce.dto.ChangePasswordRequest;
import customDesign.E_commerce.dto.UpdateAccountRequest;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.repository.UserRepository;
//import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class UserService{

    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public String changePassword(String email,  ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }
     @Transactional(readOnly = true)
     public AccountResponse getMyAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        AccountResponse response = new AccountResponse(user.getId(), user.getName(), user.getEmail(), user.getNumber(), user.getRole());
        response.setTwoFactorEnabled(user.isTwoFactorEnabled());
        response.setRoles(java.util.List.of(user.getRole().name()));
        return response;
    }

    @Transactional
    public customDesign.E_commerce.dto.TwoFactorSetupResponse enableTwoFactor(String email,
            customDesign.E_commerce.dto.EnableTwoFactorRequest request) {
        if (!"TOTP".equalsIgnoreCase(request.getMethod())) {
            throw new IllegalArgumentException("method must be TOTP");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        byte[] secretBytes = new byte[20];
        new java.security.SecureRandom().nextBytes(secretBytes);
        String secret = base32(secretBytes);
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        String qrUri = "otpauth://totp/Marketplace:" + user.getEmail() + "?secret=" + secret
                + "&issuer=Marketplace";
        return new customDesign.E_commerce.dto.TwoFactorSetupResponse(secret, qrUri);
    }

    @Transactional
    public customDesign.E_commerce.dto.TwoFactorVerifyResponse verifyTwoFactor(String email,
            customDesign.E_commerce.dto.VerifyTwoFactorRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isBlank()) {
            throw new IllegalArgumentException("Two-factor setup has not started");
        }
        if (!verifyTotp(user.getTwoFactorSecret(), request.getCode())) {
            throw new IllegalArgumentException("Invalid two-factor code");
        }
        List<String> backupCodes = new ArrayList<>();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 5; i++) {
            backupCodes.add(String.format("%04X-%04X", random.nextInt(0x10000), random.nextInt(0x10000)));
        }
        user.setTwoFactorBackupCodes(String.join(",", backupCodes));
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        return new customDesign.E_commerce.dto.TwoFactorVerifyResponse(true, backupCodes);
    }

    private static String base32(byte[] input) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder result = new StringBuilder();
        int buffer = 0, bits = 0;
        for (byte value : input) {
            buffer = (buffer << 8) | (value & 0xff);
            bits += 8;
            while (bits >= 5) {
                result.append(alphabet.charAt((buffer >>> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) result.append(alphabet.charAt((buffer << (5 - bits)) & 31));
        return result.toString();
    }

    private static boolean verifyTotp(String secret, String code) {
        try {
            byte[] key = decodeBase32(secret);
            long counter = System.currentTimeMillis() / 30000;
            for (long candidate = counter - 1; candidate <= counter + 1; candidate++) {
                byte[] data = java.nio.ByteBuffer.allocate(8).putLong(candidate).array();
                javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
                mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
                byte[] hash = mac.doFinal(data);
                int offset = hash[hash.length - 1] & 15;
                int binary = ((hash[offset] & 127) << 24) | ((hash[offset + 1] & 255) << 16)
                        | ((hash[offset + 2] & 255) << 8) | (hash[offset + 3] & 255);
                String otp = String.format("%06d", binary % 1000000);
                if (java.security.MessageDigest.isEqual(code.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        otp.getBytes(java.nio.charset.StandardCharsets.UTF_8))) return true;
            }
            return false;
        } catch (Exception ex) { throw new IllegalArgumentException("Invalid two-factor setup"); }
    }

    private static byte[] decodeBase32(String secret) {
        String normalized = secret.toUpperCase().replace("=", "");
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        int buffer = 0, bits = 0;
        for (char c : normalized.toCharArray()) {
            int value = alphabet.indexOf(c);
            if (value < 0) throw new IllegalArgumentException("Invalid TOTP secret");
            buffer = (buffer << 5) | value;
            bits += 5;
            if (bits >= 8) {
                output.write((buffer >>> (bits - 8)) & 255);
                bits -= 8;
            }
        }
        return output.toByteArray();
    }

    @Transactional
    public AccountResponse updateMyAccount(String email, UpdateAccountRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setName(req.getName().trim());
        if (req.getPhone() != null) {
            user.setNumber(Long.parseLong(req.getPhone().replaceAll("\\+", "")));
        }
        userRepository.save(user);
        return new AccountResponse( user.getId(), user.getName(), user.getEmail(), user.getNumber(), user.getRole());
    }

}
