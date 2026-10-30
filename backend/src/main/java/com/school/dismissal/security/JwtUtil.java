package com.school.dismissal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Spring Boot fat jar 中，jjwt 通过 ServiceLoader 加载实现时
     * 需要使用 LaunchedURLClassLoader，否则找不到嵌套 jar 里的 jjwt-impl。
     * Tomcat 工作线程的上下文类加载器不对，这里临时切换。
     */
    private <T> T withCorrectClassLoader(Supplier<T> action) {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            return action.get();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    public String generateToken(Integer id, String username, String role) {
        return withCorrectClassLoader(() -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", id);
            claims.put("username", username);
            claims.put("role", role);
            Date now = new Date();
            Date expiry = new Date(now.getTime() + expireHours * 3600_000L);
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expiry)
                    .signWith(key(), SignatureAlgorithm.HS256)
                    .compact();
        });
    }

    public Claims parse(String token) {
        return withCorrectClassLoader(() -> Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody());
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get();
    }
}
