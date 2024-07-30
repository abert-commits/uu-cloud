//package org.uu.manager.api;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@FeignClient(name = "uu-auth", contextId = "user-auth")
//public interface AuthClient {
//
//    @PostMapping("/generateToken")
//    OAuth2AccessToken generateToken(@RequestParam("username") String username);
//
//}
