package com.scriptplatform.security;

import com.scriptplatform.exception.BizException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // allow login / refresh
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/refresh")) {
            return true;
        }
        String token = request.getHeader(header);
        if (token == null || !token.startsWith(prefix)) {
            throw new BizException(401, "未登录或登录已过期");
        }
        token = token.substring(prefix.length()).trim();
        try {
            Claims claims = jwtUtil.parse(token);
            Long uid = claims.get("uid", Long.class);
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            UserContext.set(new LoginUser(uid, username, role));
            return true;
        } catch (Exception e) {
            log.warn("jwt parse failed: {}", e.getMessage());
            throw new BizException(401, "Token 无效或已过期");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
