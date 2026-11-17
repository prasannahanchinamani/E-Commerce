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
        Map<String, Object> claims  = new HashMap<>();
        claims.put("userName", user.getName());
        claims.put("role", user.getRole());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 30))
                .signWith(generateTokenKey())
                .compact();
    }

    public String refreshToken(User user){
        if(user == null){
            throw  new UserNotFoundException("User not found");
        }
        Map<String, Object> claims  = new HashMap<>();
        claims.put("userName", user.getName());
        claims.put("role", user.getRole());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 1))
                .signWith(generateTokenKey())
                .compact();
    }

    private Key generateTokenKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    public String ExtractEmailFromToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) generateTokenKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

    }

}