package com.scriptplatform.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.scriptplatform.entity.AuditLog;
import com.scriptplatform.security.UserContext;
import com.scriptplatform.service.AuditLogService;
import com.scriptplatform.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Records an audit log for controller methods annotated with @AuditEvent.
 * Password fields are masked in request params and response results.
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    private static final int MAX_LEN = 2048;
    private static final Pattern PWD_JSON = Pattern.compile("\"password\"\\s*:\\s*\"[^\"]*\"");

    @Resource
    private AuditLogService auditLogService;

    @Around("@annotation(com.scriptplatform.aspect.AuditEvent)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        AuditEvent event = signature.getMethod().getAnnotation(AuditEvent.class);

        AuditLog record = new AuditLog();
        record.setEventTime(LocalDateTime.now());
        record.setEventType(event.type());
        record.setRemark(event.remark());
        record.setOperator(UserContext.username());
        record.setOperatorIp(currentIp());
        record.setRequestParams(maskPassword(safeJson(buildArgs(signature, pjp.getArgs()))));

        // for login/anonymous calls, try to derive operator from request args
        if ("anonymous".equals(record.getOperator())) {
            for (Object arg : pjp.getArgs()) {
                if (arg instanceof com.scriptplatform.dto.LoginRequest) {
                    record.setOperator(((com.scriptplatform.dto.LoginRequest) arg).getUsername());
                    break;
                }
            }
        }

        Object result;
        try {
            result = pjp.proceed();
            record.setStatus(1);
            fillTarget(record, result);
            record.setResponseResult(truncate(maskPassword(safeJson(result))));
        } catch (Throwable t) {
            record.setStatus(0);
            Map<String, Object> err = new HashMap<>();
            err.put("error", t.getMessage());
            record.setResponseResult(safeJson(err));
            try {
                auditLogService.record(record);
            } catch (Exception ignore) {
            }
            throw t;
        }

        try {
            auditLogService.record(record);
        } catch (Exception e) {
            log.warn("audit record failed: {}", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> buildArgs(MethodSignature signature, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        String[] names = signature.getParameterNames();
        if (names == null) return map;
        for (int i = 0; i < names.length && i < args.length; i++) {
            Object v = args[i];
            if (v instanceof HttpServletRequest) continue;
            map.put(names[i], v);
        }
        return map;
    }

    private void fillTarget(AuditLog log, Object result) {
        try {
            if (result == null) return;
            String json = JSONUtil.toJsonStr(result);
            // try to extract data.id and data.username/script_name
            if (json.contains("\"data\"")) {
                // light-weight extraction
                Map<?, ?> map = JSONUtil.toBean(json, Map.class);
                Object data = map.get("data");
                if (data instanceof Map) {
                    Map<?, ?> dm = (Map<?, ?>) data;
                    Object id = dm.get("id");
                    if (id != null) {
                        try {
                            log.setTargetId(Long.valueOf(id.toString()));
                        } catch (NumberFormatException ignore) {
                        }
                    }
                    Object name = dm.get("username");
                    if (name == null) name = dm.get("scriptName");
                    if (name != null) log.setTargetName(String.valueOf(name));
                }
            }
        } catch (Exception ignore) {
        }
    }

    private String safeJson(Object o) {
        try {
            return o == null ? "" : JSONUtil.toJsonStr(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private String maskPassword(String src) {
        if (StrUtil.isEmpty(src)) return src;
        return PWD_JSON.matcher(src).replaceAll("\"password\":\"***\"");
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > MAX_LEN ? s.substring(0, MAX_LEN) : s;
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return IpUtil.getIp(attrs.getRequest());
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}
