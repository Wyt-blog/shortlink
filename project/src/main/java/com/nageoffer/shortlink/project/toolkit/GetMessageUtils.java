package com.nageoffer.shortlink.project.toolkit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

public class GetMessageUtils {

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

    /**
     * 获取用户操作系统
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("unix")) {
            return "Unix";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户 ip 地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
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
        return request.getRemoteAddr();
    }

    /**
     * 校验IP地址的有效性
     */
    private static boolean isValidIp(String ip) {
        // 排除内网IP和特殊IP
        return !"unknown".equalsIgnoreCase(ip)
                && !"127.0.0.1".equals(ip)
                && !"0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * 获取用户访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.toLowerCase().contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.toLowerCase().contains("opera")) {
            return "Opera";
        } else if (userAgent.toLowerCase().contains("msie") || userAgent.toLowerCase().contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问的设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }

    /**
     * 获取用户访问的网络类型
     */
    public static String getNetwork(HttpServletRequest request) {
        String actualIp = getClientIpAddress(request);
        // 这里简单判断IP地址范围，您可能需要更复杂的逻辑
        // 例如，通过调用IP地址库或调用第三方服务来判断网络类型
        return actualIp.startsWith("192.168.") || actualIp.startsWith("10.") ? "WIFI" : "Mobile";
    }

}
