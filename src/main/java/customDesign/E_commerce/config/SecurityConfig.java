package customDesign.E_commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{

       http.csrf(customizer->customizer.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/api/auth/**",  "/swagger-ui/**",  "/swagger-ui.html",
                        "/swagger-ui/**",  "/swagger-ui.html",  "/api-docs",  "/api-docs/**",  "/v3/api-docs/**",
                        "/h2-console/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                         .httpBasic(customizer->customizer.disable())
                        .sessionManagement(session->{
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                        });
        http.headers(headers ->
                headers.frameOptions(frame ->
                        frame.disable()));
        return http.build();
    }
}
