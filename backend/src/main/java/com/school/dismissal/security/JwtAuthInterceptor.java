package com.school.dismissal.security;

import com.school.dismissal.common.BusinessException;
import com.school.dismissal.common.ResultCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        try {
            Claims c = jwtUtil.parse(auth.substring(7));
            LoginUser user = new LoginUser(
                    c.get("id", Integer.class),
                    c.get("username", String.class),
                    c.get("role", String.class));
            UserContext.set(user);

            if (handler instanceof HandlerMethod hm) {
                RequireRole rr = hm.getMethodAnnotation(RequireRole.class);
                if (rr != null && Arrays.stream(rr.value()).noneMatch(r -> r.equals(user.getRole()))) {
                    throw new BusinessException(ResultCode.FORBIDDEN);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        UserContext.clear();
    }
}
