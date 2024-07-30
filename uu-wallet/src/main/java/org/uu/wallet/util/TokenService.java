//package org.uu.wallet.util;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.oauth2.provider.OAuth2Request;
//import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.HashMap;
//
//// ...
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class TokenService {
//
//    private final AuthorizationServerTokenServices tokenServices;
//
//    public OAuth2AccessToken generateTokenForExternalSystem(String clientId) {
//        // 创建 Authentication 对象，代表外部系统
//        Authentication clientAuth = new UsernamePasswordAuthenticationToken(clientId, null, Collections.emptyList());
//
//        // 准备 OAuth2 请求参数
//        HashMap<String, String> requestParameters = new HashMap<>();
//        requestParameters.put("scope", "read write");
//        requestParameters.put("grant_type", "client_credentials");
//
//        // 创建 OAuth2Request
//        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId, Collections.emptyList(), true, Collections.singleton("read_write"), null, null, null, null);
//
//        // 创建 OAuth2Authentication
//        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, clientAuth);
//
//        // 生成 token
//        return tokenServices.createAccessToken(oAuth2Authentication);
//    }
//}
