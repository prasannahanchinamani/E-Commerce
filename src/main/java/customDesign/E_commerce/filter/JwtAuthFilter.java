package customDesign.E_commerce.filter;
import customDesign.E_commerce.repository.UserRepository;
import customDesign.E_commerce.service.TokenService;
import customDesign.E_commerce.entity.User;
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

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public JwtAuthFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println(">>> FILTER HIT: " + request.getRequestURI());
        System.out.println(">>> HEADER: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                String email = tokenService.ExtractEmailFromToken(token);
                System.out.println(">>> EMAIL: " + email);
                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    var auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println(">>> AUTH SET for: " + email);
                }
            } catch (Exception ignored) {
                // expired or invalid token — context stays empty → Spring returns 401
            }
        }

        chain.doFilter(request, response);
    }
}