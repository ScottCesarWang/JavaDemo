package com.example.airportInfo.api.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-ui.html"
    );

    private final int limitPerMinute;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${security.rate-limit-per-minute:10}") int limitPerMinute) {
        this.limitPerMinute = limitPerMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        Bucket bucket = buckets.computeIfAbsent(apiKey, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"type":"about:blank","title":"Too Many Requests","status":429,"detail":"Rate limit exceeded. Max %d requests per minute."}
                    """.formatted(limitPerMinute));
        }
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limitPerMinute)
                        .refillGreedy(limitPerMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}
