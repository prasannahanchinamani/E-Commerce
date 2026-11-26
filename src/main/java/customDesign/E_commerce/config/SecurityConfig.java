package customDesign.E_commerce.config;

import customDesign.E_commerce.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/public/**",
                                "/reset-password",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/buyer/**").hasRole("BUYER")
                        .requestMatchers("/api/v1/seller/apply").hasAnyRole("BUYER","SELLER")
                        .requestMatchers("/api/v1/seller/**").hasRole("SELLER")
                        .requestMatchers("/api/v1/mediator/**").hasRole("MEDIATOR")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(h -> h.disable())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write(
                                    "{\"success\":false,\"message\":\"Login required\"}");
                        })
                )
                // check valid user and token before processing any request
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(h -> h.frameOptions(f -> f.disable()));
        return http.build();
    }
}