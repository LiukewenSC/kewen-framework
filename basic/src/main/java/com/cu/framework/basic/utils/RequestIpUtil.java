package com.cu.framework.basic.utils;


import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求IP工具
 * @author kewen
 * @since 2024-08-29
 */
public class RequestIpUtil {

    /**
     * 获取客户端真实 IP：依次尝试 x-forwarded-for、X-Forwarded-For、X-Real-IP、
     * Proxy-Client-IP、WL-Proxy-Client-IP、HTTP_CLIENT_IP、HTTP_X_FORWARDED_FOR 请求头，
     * 头不存在或值为 unknown 时继续尝试，全部取不到时返回 getRemoteAddr()
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;

    }
}
