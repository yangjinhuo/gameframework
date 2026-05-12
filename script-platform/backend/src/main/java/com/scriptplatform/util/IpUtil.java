package com.scriptplatform.util;

import javax.servlet.http.HttpServletRequest;

public class IpUtil {
    public static String getIp(HttpServletRequest request) {
        if (request == null) return "";
        String ip = request.getHeader("X-Forwarded-For");
        if (isEmpty(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (isEmpty(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (isEmpty(ip)) ip = request.getHeader("X-Real-IP");
        if (isEmpty(ip)) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip == null ? "" : ip;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty() || "unknown".equalsIgnoreCase(s);
    }
}
