package org.uu.auth.security.handler;

import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
   public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        User user =   (User)authentication.getPrincipal();
        String username =  user.getUsername();
        log.info("username="+username);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        String msg =objectMapper.writeValueAsString(RestResult.ok());
        PrintWriter printWriter = response.getWriter();
        printWriter.println(msg);
        printWriter.flush();
        printWriter.close();


    }
}
