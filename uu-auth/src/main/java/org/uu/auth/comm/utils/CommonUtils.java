package org.uu.auth.comm.utils;

import cn.hutool.core.util.StrUtil;
import org.apache.logging.log4j.util.Strings;
import org.uu.common.core.constant.SecurityConstants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CommonUtils {

    public static String getOAuth2ClientId() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String clientId = request.getParameter(SecurityConstants.CLIENT_ID_KEY);
        if (StrUtil.isNotBlank(clientId)) {
            return clientId;
        }

        String basic = request.getHeader(SecurityConstants.AUTHORIZATION_KEY);
        if (StrUtil.isNotBlank(basic) && basic.startsWith(SecurityConstants.BASIC_PREFIX)) {
            basic = basic.replace(SecurityConstants.BASIC_PREFIX, Strings.EMPTY);
            String basicPlainText = new String(Base64.getDecoder().decode(basic.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            clientId = basicPlainText.split(":")[0];
        }
        return clientId;
    }
}
