package org.uu.wallet.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * IP地址工具类
 */
public class IpUtil {

    /**
     * 获取真实的IP地址
     *
     * @param request HttpServletRequest
     * @return 真实IP地址
     */
    public static String getRealIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP。
            int index = ip.indexOf(',');
            return index != -1 ? ip.substring(0, index) : ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 如果都没有，则使用request.getRemoteAddr获取IP
        return request.getRemoteAddr();
    }


    /**
     * 校验IP是否在白名单中
     * @param clientIp 客户端IP
     * @param whiteList 白名单IP字符串，IP之间以逗号分隔
     * @return 是否在白名单中
     */
    public static boolean validateClientIp(String clientIp, String whiteList) {
        if (clientIp == null || whiteList == null || whiteList.isEmpty()) {
            return false;
        }

        // 使用HashSet进行存储，提高查找效率
        Set<String> whiteListSet = new HashSet<>(Arrays.asList(whiteList.split(",")));
        return whiteListSet.contains(clientIp.trim());
    }
}
