package com.nageoffer.shortlink.project.toolkit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

public class IpUtil {
    // 定义可能包含真实IP的请求头
    private static final List<String> IP_HEADER_NAMES = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    );

    public static String getClientIpAddress(HttpServletRequest request) {
        // 遍历请求头，查找真实IP
        for (String headerName : IP_HEADER_NAMES) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null && !headerValue.trim().isEmpty()) {
                // 处理形如 "X-Forwarded-For: client, proxy1, proxy2" 的情况
                String[] ips = headerValue.split(",");
                for (String ip : ips) {
                    String trimmedIp = ip.trim();
                    if (isValidIp(trimmedIp)) {
                        return trimmedIp;
                    }
                }
            }
        }
        // 如果没找到，就返回原始的远程地址
        return request.getRemoteAddr();
    }

    // 校验IP地址的有效性
    private static boolean isValidIp(String ip) {
        // 排除内网IP和特殊IP
        return !"unknown".equalsIgnoreCase(ip)
                && !"127.0.0.1".equals(ip)
                && !"0:0:0:0:0:0:0:1".equals(ip);
    }
}