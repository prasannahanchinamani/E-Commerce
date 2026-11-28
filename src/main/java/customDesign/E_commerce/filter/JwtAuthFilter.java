package customDesign.E_commerce.filter;
import customDesign.E_commerce.repository.UserRepository;
import customDesign.E_commerce.repository.RefreshTokenRepository;
import customDesign.E_commerce.service.TokenService;
import customDesign.E_commerce.entity.User;
import customDesign.E_commerce.entity.RefreshToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
/*
web authentication  Details source class use that its easy and not only mail check all user details
 */


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtAuthFilter(TokenService tokenService, UserRepository userRepository,
                         RefreshTokenRepository refreshTokenRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        org.slf4j.Logger jwtLogger = org.slf4j.LoggerFactory.getLogger(JwtAuthFilter.class);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                String email = tokenService.ExtractEmailFromToken(token);
                if (!tokenService.isAccessToken(token)) {
                    jwtLogger.warn("Request rejected: bearer credential is not an access token, uri={}", request.getRequestURI());
                    chain.doFilter(request, response);
                    return;
                }
                String sessionId = tokenService.extractSessionId(token);
                RefreshToken session = refreshTokenRepository.findSessionBySessionId(sessionId).orElse(null);
                Instant now = Instant.now();
                if (session == null || session.isRevoked() || session.getSessionId() == null
                        || session.getLastActivityAt() == null || session.getExpiresAt().isBefore(now)
                        || session.getLastActivityAt().plus(Duration.ofMinutes(10)).isBefore(now)) {
                    if (session != null && !session.isRevoked()) {
                        session.setRevoked(true);
                        refreshTokenRepository.save(session);
                    }
                    chain.doFilter(request, response);
                    return;
                }
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null || !user.getId().equals(session.getUser().getId())) {
                    chain.doFilter(request, response);
                    return;
                }
                if (user != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    var auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))

                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    session.setLastActivityAt(now);
                    refreshTokenRepository.save(session);
                }
            } catch (Exception ex) {
                jwtLogger.warn("Invalid or expired bearer token, uri={}", request.getRequestURI());
            }
        }

        chain.doFilter(request, response);
    }
}