package customDesign.E_commerce.service;

import customDesign.E_commerce.Exceptions.UserNotFoundException;
import customDesign.E_commerce.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(User user){
        if(user == null){
            throw  new UserNotFoundException("User not found");
        }
        return buildToken(user, "access", 1000L * 60 * 15);
    }

    public String refreshToken(User user){
        return buildToken(user, "refresh", 1000L * 60 * 60 * 24 * 7);
    }

    private String buildToken(User user, String tokenType, long ttlMillis) {
        if(user == null){
            throw new UserNotFoundException("User not found");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userName", user.getName());
        claims.put("role", user.getRole().name());
        claims.put("tokenType", tokenType);
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(generateTokenKey(), Jwts.SIG.HS512)
                .compact();
    }

    // change return type from Key to SecretKey
    private SecretKey generateTokenKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] fixedKey = new byte[64];  // 64 bytes = 512 bits for HS512
        System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.length, 64));
        return Keys.hmacShaKeyFor(fixedKey);
    }
    public String ExtractEmailFromToken(String token){
        return Jwts.parser()
                .verifyWith(generateTokenKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isAccessToken(String token) {
        return isTokenType(token, "access");
    }

    public boolean isRefreshToken(String token) {
        return isTokenType(token, "refresh");
    }

    private boolean isTokenType(String token, String expectedType) {
        try {
            Object tokenType = Jwts.parser()
                    .verifyWith(generateTokenKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("tokenType");
            return expectedType.equals(tokenType);
        } catch (Exception ex) {
            return false;
        }
    }

}