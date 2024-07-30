package org.uu.manager.interceptor;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.uu.common.core.result.RestResult;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.enums.ClientTypeEnum;
import org.uu.manager.service.impl.SysWhiteServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;


@Slf4j
public class WhiteListInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        log.info("机主的ip是" + ipAddress);
        SysWhiteServiceImpl sysWhiteServiceImpl = null;
        WebApplicationContext cxt = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
        if (cxt != null && cxt.getBean(SysWhiteServiceImpl.class) != null) {
            sysWhiteServiceImpl = cxt.getBean(SysWhiteServiceImpl.class);
        }
        String currentClientId = UserContext.getCurrentClientId();
        String clientType = ClientTypeEnum.getTypeByClientCode(currentClientId);
        //根据来源获取白名单池并判断
        if (sysWhiteServiceImpl.getIp(String.valueOf(ipAddress), clientType)) {
            return true;
        } else {
            log.info("机主的ip是1" + ipAddress);
            returnJson(response, JSON.toJSONString(RestResult.failed("ip不在白名单之内")));
            return false;
        }
    }


    private void returnJson(HttpServletResponse response, String result) {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(result);
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
