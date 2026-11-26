package customDesign.E_commerce.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiRequestLogFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiRequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String principal = authentication == null ? "anonymous" : authentication.getName();
            String authorities = authentication == null ? ""
                    : authentication.getAuthorities().toString();
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

            log.info("api_request method={} uri={} status={} principal={} authorities={} durationMs={} requestId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    principal,
                    authorities,
                    durationMs,
                    requestId);
            MDC.clear();
        }
    }
}
