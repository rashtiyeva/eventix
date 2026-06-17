package org.eventix.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


@Slf4j
@Component("customRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String ip = extractIp(request);
            String userAgent = request.getHeader("User-Agent");

            String deviceId = request.getHeader("X-Device-Id");

            RequestContext.setIp(ip);
            RequestContext.setUserAgent(userAgent);
            RequestContext.setDeviceId(deviceId);

            filterChain.doFilter(request, response);

        } finally {
            RequestContext.clear();
        }
    }

    private String extractIp(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}