package com.gyan.util;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Duration AUTH_WINDOW = Duration.ofMinutes(1);
    private static final Duration AI_WINDOW = Duration.ofMinutes(1);
    private static final int AUTH_LIMIT = 12;
    private static final int AI_LIMIT = 20;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String path = request.getRequestURI();
        RateLimitDefinition definition = getDefinition(path);

        if (definition == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = definition.prefix + ":" + resolveSubject(request, definition.prefix);
        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(definition.window));

        if (!counter.tryAcquire(definition.limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(definition.message);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitDefinition getDefinition(String path) {
        if (path.startsWith("/auth/login") || path.startsWith("/auth/register") || path.startsWith("/auth/refresh") || path.startsWith("/auth/logout")) {
            return new RateLimitDefinition("auth", AUTH_LIMIT, AUTH_WINDOW, "Too many authentication requests. Please wait a minute and try again.");
        }

        if (path.startsWith("/ai/")) {
            return new RateLimitDefinition("ai", AI_LIMIT, AI_WINDOW, "Too many AI requests. Please slow down and try again shortly.");
        }

        return null;
    }

    private String resolveSubject(HttpServletRequest request, String prefix) {
        if ("ai".equals(prefix)) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
                return authentication.getName();
            }
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private record RateLimitDefinition(String prefix, int limit, Duration window, String message) {
    }

    private static final class WindowCounter {
        private final Duration window;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile Instant windowStartedAt = Instant.now();

        private WindowCounter(Duration window) {
            this.window = window;
        }

        private synchronized boolean tryAcquire(int limit) {
            Instant now = Instant.now();
            if (windowStartedAt.plus(window).isBefore(now)) {
                windowStartedAt = now;
                count.set(0);
            }

            return count.incrementAndGet() <= limit;
        }
    }
}
