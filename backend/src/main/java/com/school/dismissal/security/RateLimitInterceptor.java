package com.school.dismissal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.dismissal.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final long WINDOW_MS = 60_000L;

    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    private static class Window {
        final long start = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
    }

    private int limitFor(String path, String method) {
        if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) return 20;
        if (path.startsWith("/api/users") || path.startsWith("/api/classes")
                || path.startsWith("/api/students") || path.startsWith("/api/dismissal")
                || path.startsWith("/api/notices")) return 50;
        return 200;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws IOException {
        int limit = limitFor(req.getRequestURI(), req.getMethod());
        String key = req.getRemoteAddr() + "|" + req.getRequestURI();
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(e -> now - e.getValue().start > WINDOW_MS);
        Window w = buckets.compute(key, (k, v) -> (v == null || now - v.start > WINDOW_MS) ? new Window() : v);
        int c = w.count.incrementAndGet();
        if (c > limit) {
            resp.setStatus(429);
            resp.setContentType("application/json;charset=UTF-8");
            String msg = "/api/auth/login".equals(req.getRequestURI())
                    ? "登录尝试过于频繁，请 1 分钟后再试"
                    : "操作过于频繁，请稍后再试";
            resp.getWriter().write(new ObjectMapper().writeValueAsString(Result.error(429, msg)));
            return false;
        }
        return true;
    }
}
