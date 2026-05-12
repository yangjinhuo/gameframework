package com.scriptplatform.security;

import com.scriptplatform.exception.BizException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleAspect {

    @Around("@annotation(com.scriptplatform.security.RequireRole) || @within(com.scriptplatform.security.RequireRole)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        RequireRole ann = AnnotationUtils.findAnnotation(sig.getMethod(), RequireRole.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(sig.getMethod().getDeclaringClass(), RequireRole.class);
        }
        if (ann != null) {
            String role = UserContext.role();
            if (role == null) {
                throw new BizException(401, "未登录");
            }
            boolean ok = false;
            for (String r : ann.value()) {
                if (r.equalsIgnoreCase(role)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                throw new BizException(403, "无权限执行此操作");
            }
        }
        return pjp.proceed();
    }
}
