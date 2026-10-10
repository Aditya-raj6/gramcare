package com.healthharbor.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Validates the bearer token on every request, enforces {@link AccessPolicy} and forwards the
 * caller identity to downstream services as X-User-* headers.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${healthharbor.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        if (HttpMethod.OPTIONS.equals(request.getMethod()) || !path.startsWith("/api/") || AccessPolicy.isPublic(path)) {
            return chain.filter(exchange);
        }
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        String role = claims.get("role", String.class);
        if (!AccessPolicy.isAllowed(request.getMethod(), path, role)) {
            log.info("Denied {} {} for role {}", request.getMethod(), path, role);
            return reject(exchange, HttpStatus.FORBIDDEN, "Role " + role + " is not permitted to perform this action");
        }
        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", String.valueOf(claims.get("uid")))
                .header("X-User-Name", claims.getSubject())
                .header("X-User-FullName", String.valueOf(claims.get("name")))
                .header("X-User-Role", role)
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, exchange.getRequest().getPath());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
