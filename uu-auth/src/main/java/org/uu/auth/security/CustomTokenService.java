package org.uu.auth.security;


import com.alibaba.fastjson.JSONObject;
import com.nimbusds.jose.JWSObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.auth.comm.enums.AuthenticationModeEnum;
import org.uu.auth.comm.rabbitmq.LoginLogMessageSender;
import org.uu.auth.security.details.member.MemberDetailsServiceImpl;
import org.uu.auth.security.util.RsaUtil;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.SecurityConstants;
import org.uu.common.core.enums.ClientTypeEnum;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.core.utils.HashingUtils;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.core.utils.UserAgentUtil;
import org.uu.common.redis.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomTokenService {

    @Autowired
    private AuthorizationServerTokenServices tokenServices;

    private final RedisTemplate redisTemplate;
    private final MemberDetailsServiceImpl memberDetailsServiceImpl;
    private final LoginLogMessageSender loginLogMessageSender;
    private final RedisUtils redisUtils;

    /**
     * 模拟生成 token
     *
     * @param data
     * @return {@link String}
     */
    public String generateTokenWithoutPasswordCheck(String data, HttpServletRequest request) {

        //这是php服务器的ip
        String realIP = request.getHeader("cip");

//        String realIP = IpUtil.getRealIP(request);

        log.info("模拟生成token, 请求ip: {}", realIP);

        byte[] decodedKey = Base64.getDecoder().decode("wz+glqDb2YceJ3piABkWig==");
        SecretKeySpec reqKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        //解密用户名
        String username = null;
        try {
            username = RsaUtil.decryptData(data, reqKey);
        } catch (Exception e) {
            log.error("模拟生成 token失败, 解密用户名失败, 请求ip: {}, e: {}", realIP, e.getMessage());
            return null;
        }

        if (StringUtils.isEmpty(username)) {
            log.error("模拟生成 token失败, 解密用户名失败, 请求ip: {}", realIP);
            return null;
        }

        //获取用户信息 (创建一个具有用户名和权限的UserDetails对象)
        UserDetails userDetails = memberDetailsServiceImpl.loadUserByUsername(username);

        if (userDetails == null) {
            log.error("模拟生成 token失败, 该用户不存在, 请求ip: {}, 用户名: {}", realIP, username);
            //未启用
            return null;
        }

        if (!userDetails.isEnabled()) {
            log.error("模拟生成 token失败, 该用户已被禁用, 请求ip: {}, 用户名: {}", realIP, username);
            //未启用
            return null;
        }

        // 创建一个Authentication对象
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 将Authentication对象设置到SecurityContext中
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 创建 OAuth2 请求
        OAuth2Request oAuth2Request = new OAuth2Request(null, "member", userDetails.getAuthorities(), true, Collections.singleton("member"), null, null, null, null);

        // 创建 OAuth2 认证对象
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        // 生成并返回令牌
        OAuth2AccessToken accessToken = tokenServices.createAccessToken(oAuth2Authentication);

        String token = accessToken.getValue();
        String refreshToken = accessToken.getRefreshToken().getValue();

        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(token);
        } catch (Exception e) {
            log.error("模拟生成token失败, 请求ip: {}, username: {}", realIP, username);
            log.error(e.getMessage());
            return null;
        }

        String payload = jwsObject.getPayload().toString();

        JSONObject jsonObject = JSONObject.parseObject(payload);

        String jti = jsonObject.getString("jti");
        long exp = jsonObject.getLong("exp");


        //将 jti存入reids
        redisTemplate.opsForValue().set(SecurityConstants.LOGIN_USER_ID + jsonObject.get("username"), jti);

        try {
            //存储 refresh_token 到redis
            String encryptedToken = HashingUtils.md5Hash(String.valueOf(accessToken.getRefreshToken())); // 加密Refresh Token
            redisTemplate.opsForValue().set(SecurityConstants.REFRESH_TOKEN_PREFIX + jsonObject.get("username"), encryptedToken, 7, TimeUnit.DAYS);
        } catch (NoSuchAlgorithmException e) {
            log.error("模拟生成 token失败: 存储 refresh_token 到redis失败");
            return null;
        }

        //商户会员登录成功 记录登录日志
        loginLogMessageSender.recordLoginLog(
                jsonObject.getLong("userId"),
                jsonObject.getString("username"),
                LocalDateTime.now(),
                realIP,
                UserAgentUtil.getDeviceType(request.getHeader("user-agent")),
                request.getHeader("user-agent"),
                AuthenticationModeEnum.MERCHANT_LOGIN.getCode(),
                jsonObject.getString("memberType"),
                jsonObject.getString("firstLoginIp")
        );

        try {
            // 1. 动态生成一个AES密钥
            SecretKey aesKey = RsaUtil.generateAESKey();

            // 2. 使用AES密钥加密数据
            String encryptedData = RsaUtil.encryptData(token, aesKey);
            // 2. 使用AES密钥加密 refreshToken
            String encryptedData2 = RsaUtil.encryptData(refreshToken, aesKey);

            //将密钥和token存入到Redis  过期时间为 15秒  而不是通过接口返回
            JSONObject generatetoken = new JSONObject();
            generatetoken.put("aesKey", serializeKey(aesKey));
            generatetoken.put("encryptedData", encryptedData);
            generatetoken.put("encryptedData2", encryptedData2);

            log.info("模拟生成token成功, username: {}, 请求ip: {}", username, realIP);

            redisTemplate.opsForValue().set("GENERATETOKEN:" + username, generatetoken, 300, TimeUnit.SECONDS);
            CommonUtils.insertToken(username, redisUtils, GlobalConstants.timeOut);
            return "1";
        } catch (Exception e) {
            log.error("模拟生成token失败, 请求ip: {}, 用户名: {} e: {}", realIP, username, e.getMessage());
            return null;
        }
    }

    // 将AES密钥序列化为Base64字符串
    public static String serializeKey(SecretKey secretKey) {
        byte[] keyData = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(keyData);
    }



    /**
     * 模拟生成 token
     *
     * @param data
     * @return {@link String}
     */
    public String generateAppTokenWithoutPasswordCheck(String data, HttpServletRequest request) {

        //这是php服务器的ip
        String realIP = request.getHeader("cip");

//        String realIP = IpUtil.getRealIP(request);

        log.info("模拟生成token, 请求ip: {}", realIP);

        byte[] decodedKey = Base64.getDecoder().decode("wz+glqDb2YceJ3piABkWig==");
        SecretKeySpec reqKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        //解密用户名
        String username = null;
        try {
            username = RsaUtil.decryptData(data, reqKey);
        } catch (Exception e) {
            log.error("模拟生成 token失败, 解密用户名失败, 请求ip: {}, e: {}", realIP, e.getMessage());
            return null;
        }

        if (StringUtils.isEmpty(username)) {
            log.error("模拟生成 token失败, 解密用户名失败, 请求ip: {}", realIP);
            return null;
        }

        //获取用户信息 (创建一个具有用户名和权限的UserDetails对象)
        UserDetails userDetails = memberDetailsServiceImpl.loadUserByUsername(username);

        if (userDetails == null){
            log.error("模拟生成 token失败, 该用户不存在, 请求ip: {}, 用户名: {}", realIP, username);
            //未启用
            return null;
        }

        if (!userDetails.isEnabled()){
            log.error("模拟生成 token失败, 该用户已被禁用, 请求ip: {}, 用户名: {}", realIP, username);
            //未启用
            return null;
        }

        // 创建一个Authentication对象
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 将Authentication对象设置到SecurityContext中
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 创建 OAuth2 请求
        OAuth2Request oAuth2Request = new OAuth2Request(null, "member", userDetails.getAuthorities(), true, Collections.singleton("member"), null, null, null, null);

        // 创建 OAuth2 认证对象
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        // 生成并返回令牌
        OAuth2AccessToken accessToken = tokenServices.createAccessToken(oAuth2Authentication);

        String token = accessToken.getValue();
        String refreshToken = accessToken.getRefreshToken().getValue();

        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(token);
        } catch (Exception e) {
            log.error("模拟生成token失败, 请求ip: {}, username: {}", realIP, username);
            log.error(e.getMessage());
            return null;
        }

        String payload = jwsObject.getPayload().toString();

        JSONObject jsonObject = JSONObject.parseObject(payload);

        String jti = jsonObject.getString("jti");
        long exp = jsonObject.getLong("exp");


        //将 jti存入reids
        redisTemplate.opsForValue().set(SecurityConstants.LOGIN_USER_ID + jsonObject.get("username"), jti);

        try {
            //存储 refresh_token 到redis
            String encryptedToken = HashingUtils.md5Hash(String.valueOf(accessToken.getRefreshToken())); // 加密Refresh Token
            redisTemplate.opsForValue().set(SecurityConstants.REFRESH_TOKEN_PREFIX + jsonObject.get("username"), encryptedToken, 7, TimeUnit.DAYS);
        } catch (NoSuchAlgorithmException e) {
            log.error("模拟生成 token失败: 存储 refresh_token 到redis失败");
            return null;
        }

        //商户会员登录成功 记录登录日志
        loginLogMessageSender.recordLoginLog(
                jsonObject.getLong("userId"),
                jsonObject.getString("username"),
                LocalDateTime.now(),
                realIP,
                UserAgentUtil.getDeviceType(request.getHeader("user-agent")),
                request.getHeader("user-agent"),
                AuthenticationModeEnum.MERCHANT_LOGIN.getCode(),
                jsonObject.getString("memberType"),
                jsonObject.getString("firstLoginIp")
        );

        try {
            // 1. 动态生成一个AES密钥
            SecretKey aesKey = RsaUtil.generateAESKey();

            // 2. 使用AES密钥加密数据
            String encryptedData = RsaUtil.encryptData(token, aesKey);
            // 2. 使用AES密钥加密 refreshToken
            String encryptedData2 = RsaUtil.encryptData(refreshToken, aesKey);

            //将密钥和token存入到Redis  过期时间为 15秒  而不是通过接口返回
            JSONObject generatetoken = new JSONObject();
            generatetoken.put("aesKey", serializeKey(aesKey));
            generatetoken.put("encryptedData", encryptedData);
            generatetoken.put("encryptedData2", encryptedData2);

            log.info("模拟生成token成功, username: {}, 请求ip: {}", username, realIP);

            redisTemplate.opsForValue().set("GENERATETOKEN:" + username, generatetoken, 300, TimeUnit.SECONDS);
            CommonUtils.insertToken(username, redisUtils, GlobalConstants.timeOut);
            return "1";
        } catch (Exception e) {
            log.error("模拟生成token失败, 请求ip: {}, 用户名: {} e: {}", realIP, username);
            return null;
        }
    }


    /**
     * 钱包注册自动登录
     *
     * @param data
     * @param request
     * @return {@link String}
     */
    public String generateTokenForWallet(String data, HttpServletRequest request) {

        String realIP = request.getHeader("cip");

        log.info("模拟生成token, 请求ip: {}", realIP);

        byte[] decodedKey = Base64.getDecoder().decode("wz+glqDb2YceJ3piABkWig==");
        SecretKeySpec reqKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        //解密用户名
        String username = null;
        try {
            username = RsaUtil.decryptData(data, reqKey);
        } catch (Exception e) {
            log.error("模拟生成 token失败, 解密用户名失败, 请求ip: {}, e: {}", realIP, e.getMessage());
            return null;
        }

        if (StringUtils.isEmpty(username)) {
            log.error("模拟生成 token失败, 解密用户名失败, 请求ip: {}", realIP);
            return null;
        }

        //获取用户信息 (创建一个具有用户名和权限的UserDetails对象)
        UserDetails userDetails = memberDetailsServiceImpl.loadUserByUsername(username);



        if (userDetails == null) {
            log.error("模拟生成 token失败, 该用户不存在, 请求ip: {}, 用户名: {}", realIP, username);
            //未启用
            return null;
        }

        if (!userDetails.isEnabled()) {
            log.error("模拟生成 token失败, 该用户已被禁用, 请求ip: {}, 用户名: {}", realIP, username);
            //未启用
            return null;
        }

        // 创建一个Authentication对象
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 将Authentication对象设置到SecurityContext中
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 创建 OAuth2 请求
        OAuth2Request oAuth2Request = new OAuth2Request(null, "member", userDetails.getAuthorities(), true, Collections.singleton("member"), null, null, null, null);

        // 创建 OAuth2 认证对象
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        // 生成并返回令牌
        OAuth2AccessToken accessToken = tokenServices.createAccessToken(oAuth2Authentication);

        String token = accessToken.getValue();
        String refreshToken = accessToken.getRefreshToken().getValue();

        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(token);
        } catch (Exception e) {
            log.error("模拟生成token失败, 请求ip: {}, username: {}", realIP, username);
            log.error(e.getMessage());
            return null;
        }

        String payload = jwsObject.getPayload().toString();

        JSONObject jsonObject = JSONObject.parseObject(payload);

        String jti = jsonObject.getString("jti");
        long exp = jsonObject.getLong("exp");


        //将 jti存入reids
        redisTemplate.opsForValue().set(SecurityConstants.LOGIN_USER_ID + jsonObject.get("username"), jti);

        try {
            //存储 refresh_token 到redis
            String encryptedToken = HashingUtils.md5Hash(String.valueOf(accessToken.getRefreshToken())); // 加密Refresh Token
            redisTemplate.opsForValue().set(SecurityConstants.REFRESH_TOKEN_PREFIX + jsonObject.get("username"), encryptedToken, 7, TimeUnit.DAYS);
        } catch (NoSuchAlgorithmException e) {
            log.error("模拟生成 token失败: 存储 refresh_token 到redis失败");
            return null;
        }

        //钱包会员登录成功 记录登录日志
        loginLogMessageSender.recordLoginLog(
                jsonObject.getLong("userId"),
                jsonObject.getString("username"),
                LocalDateTime.now(),
                realIP,
                UserAgentUtil.getDeviceType(request.getHeader("user-agent")),
                request.getHeader("user-agent"),
                AuthenticationModeEnum.MEMBER_LOGIN.getCode(),
                jsonObject.getString("memberType"),
                jsonObject.getString("firstLoginIp")
        );

        try {
            // 1. 动态生成一个AES密钥
            SecretKey aesKey = RsaUtil.generateAESKey();

            // 2. 使用AES密钥加密数据
            String encryptedData = RsaUtil.encryptData(token, aesKey);
            // 2. 使用AES密钥加密 refreshToken
            String encryptedData2 = RsaUtil.encryptData(refreshToken, aesKey);

            //将密钥和token存入到Redis  过期时间为 15秒  而不是通过接口返回
            JSONObject generatetoken = new JSONObject();
            generatetoken.put("aesKey", serializeKey(aesKey));
            generatetoken.put("encryptedData", encryptedData);
            generatetoken.put("encryptedData2", encryptedData2);

            log.info("前台注册, 模拟生成token成功, username: {}, 请求ip: {}", username, realIP);

            redisTemplate.opsForValue().set("GENERATETOKEN:" + username, generatetoken, 300, TimeUnit.SECONDS);
            CommonUtils.insertToken(username, redisUtils, GlobalConstants.timeOut);
            return "1";
        } catch (Exception e) {
            log.error("前台注册, 模拟生成token失败, 请求ip: {}, 用户名: {} e: {}", realIP, username, e.getMessage());
            return null;
        }

    }
}
