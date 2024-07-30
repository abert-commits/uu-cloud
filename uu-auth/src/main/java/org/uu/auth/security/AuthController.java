package org.uu.auth.security;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.auth.comm.enums.AuthenticationModeEnum;
import org.uu.auth.comm.enums.ClientEnums;
import org.uu.auth.comm.property.ArProperty;
import org.uu.auth.comm.rabbitmq.LoginLogMessageSender;
import org.uu.auth.comm.utils.IpUtil;
import org.uu.auth.comm.utils.TokenUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.constant.SecurityConstants;
import org.uu.common.core.enums.ClientTypeEnum;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.result.UnauthorizedResponse;
import org.uu.common.core.utils.*;
import org.uu.common.pay.api.MerchantFeignClient;
import org.uu.common.pay.api.OAuthClientFeignClient;
import org.uu.common.pay.api.UserFeignClient;
import org.uu.common.pay.dto.OAuth2ClientDTO;
import org.uu.common.pay.dto.UserAuthDTO;
import org.uu.common.redis.util.RedisUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.util.ObjectUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/oauth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final RedisTemplate redisTemplate;
    private final TokenEndpoint tokenEndpoint;
    private final RedisUtils redisUtils;
    private final CustomTokenService customTokenService;
    private KeyPair keyPair;
    private final LoginLogMessageSender loginLogMessageSender;
    private final MerchantFeignClient merchantFeignClient;
    private final UserFeignClient userFeignClient;
    private final ArProperty arProperty;
    private final OAuthClientFeignClient oAuthClientFeignClient;

    @PostMapping("/token")
    public Object postAccessToken(Principal principal, @RequestParam Map<String, String> parameters, HttpServletRequest request) throws HttpRequestMethodNotSupportedException {

        // 判断是否是刷新令牌的请求
        if ("refresh_token".equals(parameters.get("grant_type"))) {
            // 这里添加刷新令牌的逻辑
            String refreshTokenValue = parameters.get("refresh_token"); // 通常refresh_token通过请求体传递
            if (refreshTokenValue == null) {
                return RestResult.relogin(ResultCode.RELOGIN);
            }

            OAuth2AccessToken newAccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();


            String token = newAccessToken.getValue();

            try {
                JWSObject jwsObject = JWSObject.parse(token);
                String payload = jwsObject.getPayload().toString();


                JSONObject jsonObject = JSONObject.parseObject(payload);
                String jti = jsonObject.getString("jti");
                long exp = jsonObject.getLong("exp");

                String username = jsonObject.get("username") != null ? jsonObject.getString("username") : jsonObject.getString("user_name");

                //获取当前时间戳(秒)
                long currentTimeSeconds = System.currentTimeMillis() / 1000;

                //获取用户最后活动时间
                Object value = redisTemplate.opsForValue().get(SecurityConstants.LAST_ACTIVITY_TIME_PREFIX + username);
                Long lastActivityTime = null;

                if (value instanceof Number) {
                    lastActivityTime = ((Number) value).longValue();
                }

                //用户最后活动时间不是null并且距离现在小于1个小时内
                boolean isActivityWithinHour = lastActivityTime != null && (currentTimeSeconds - lastActivityTime) <= SecurityConstants.TOKEN_EXPIRATION_TIME_IN_SECONDS; // 3600秒为1小时

                //如果用户最后活动时间 超过1小时了 让会员重新登录
                if (!isActivityWithinHour){
                    return ResponseEntity.status(200).body(UnauthorizedResponse.create());
                }

                // 加密客户端传过来的refresh_token
                String encryptedClientToken = HashingUtils.md5Hash(refreshTokenValue);

                // 从Redis获取存储的refresh_token 使用Optional简化null检查
                Optional<String> storedEncryptedTokenOpt = Optional.ofNullable((String) redisTemplate.opsForValue().get(SecurityConstants.REFRESH_TOKEN_PREFIX + username));

                // 检查存储的refresh_token 是否存在并与传入的refresh_token 匹配
                boolean tokenValid = storedEncryptedTokenOpt.map(storedToken -> storedToken.equals(encryptedClientToken)).orElse(false);

                // 如果refresh_token 不存在或不一致(被重新登录了)，则返回401状态
                if (!tokenValid) {
                    return ResponseEntity.status(401).body(UnauthorizedResponse.create());
                }

                //存储 refresh_token 到redis
                String encryptedToken = HashingUtils.md5Hash(String.valueOf(newAccessToken.getRefreshToken())); // 加密Refresh Token
                redisTemplate.opsForValue().set(SecurityConstants.REFRESH_TOKEN_PREFIX + jsonObject.get("username"), encryptedToken, 7, TimeUnit.DAYS);


                //将新的token jti存入到redis
                redisTemplate.opsForValue().set(SecurityConstants.LOGIN_USER_ID + username, jti,SecurityConstants.TOKEN_EXPIRATION_TIME_IN_SECONDS,TimeUnit.SECONDS);

                return ResponseEntity.ok(newAccessToken);
            }catch (Exception e){
                return RestResult.relogin(ResultCode.RELOGIN);
            }
        }
        //获取环境信息
        String appEnv = arProperty.getAppEnv();
        boolean isTestEnv = "sit".equals(appEnv) || "dev".equals(appEnv);

        String ip = IpUtil.getRealIP(request);
        boolean inWhiteList = backgroundLoginWhitelistVerification(ip, principal);

        if(!inWhiteList){
            return RestResult.failed("ip不在白名单之内");
        }
        String userName = parameters.get("username");
        JSONObject resultObj = new JSONObject();
        RestResult<UserAuthDTO> result = null;
        if (StringUtils.isEmpty(userName)) {
            return RestResult.failed("Username can not be empty");
        }

        if(principal.getName().equals("uu")){
            result = userFeignClient.getUserByUsername(parameters.get("username"));
            if(ObjectUtils.isEmpty(result.getData())) return RestResult.failed("user does not exist");
            if(result.getData().getIsBindGoogle().equals(1) && StringUtils.isEmpty(parameters.get("totpCode"))){
                return  RestResult.failed("Google verification cannot be empty");
            }else if(result.getData().getIsBindGoogle().equals(1) && !StringUtils.isEmpty(parameters.get("totpCode"))){
                if (!isTestEnv) {
                    boolean isCodeValid =  GoogleAuthenticatorUtil.checkCode(result.getData().getGooglesecret(),Long.parseLong(parameters.get("totpCode")),System.currentTimeMillis());
                    if(!isCodeValid){
                        return  RestResult.failed("Google verification code is incorrect");
                    }
                }
                resultObj.put("isBindGoogle", 1);
            }else if(result.getData().getIsBindGoogle().equals(0)){
                resultObj.put("isBindGoogle", 0);
                resultObj.put("googlesecret", result.getData().getGooglesecret());
            }

        }else if(principal.getName().equals("merchant")){
            result = merchantFeignClient.getMerchantByUsername(parameters.get("username"));
            if(ObjectUtils.isEmpty(result.getData())) return RestResult.failed("user does not exist");
            if(result.getData().getIsBindGoogle().equals(1) && StringUtils.isEmpty(parameters.get("totpCode"))){
                return  RestResult.failed("Google verification cannot be empty");
            }else if(result.getData().getIsBindGoogle().equals(1) && !StringUtils.isEmpty(parameters.get("totpCode"))){
                if (!isTestEnv) {
                    boolean isCodeValid =  GoogleAuthenticatorUtil.checkCode(result.getData().getGooglesecret(),Long.parseLong(parameters.get("totpCode")),System.currentTimeMillis());
                    if(!isCodeValid){
                        return  RestResult.failed("Google verification code is incorrect");
                    }
                }
                resultObj.put("isBindGoogle", 1);
            }else if(result.getData().getIsBindGoogle().equals(0)){
                resultObj.put("isBindGoogle", 0);
                resultObj.put("googlesecret", result.getData().getGooglesecret());
            }
        }else if(principal.getName().equals("member")){
            CommonUtils.insertToken(userName, redisUtils, GlobalConstants.timeOut);
        }


        redisTemplate.opsForHash().put(SecurityConstants.LOGIN_USER_NAME + userName, SecurityConstants.LOGIN_LAST_LOGIN_TIME, DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT));
        redisTemplate.opsForHash().put(SecurityConstants.LOGIN_USER_NAME + userName, SecurityConstants.LOGIN_LAST_LOGIN_IP, ip);
        redisTemplate.opsForHash().increment(SecurityConstants.LOGIN_USER_NAME + userName, SecurityConstants.LOGIN_COUNT, 1);
        OAuth2AccessToken accessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        resultObj.put("accessToken", accessToken);
        try {
            long currentTimeSeconds = System.currentTimeMillis() / 1000;

            String token = accessToken.getValue();
            JWSObject jwsObject = JWSObject.parse(token);
            String payload = jwsObject.getPayload().toString();


            JSONObject jsonObject = JSONObject.parseObject(payload);
            String jti = jsonObject.getString("jti");
            long exp = jsonObject.getLong("exp");

            redisTemplate.opsForValue().set(SecurityConstants.LOGIN_USER_ID + jsonObject.get("username"), jti,SecurityConstants.TOKEN_EXPIRATION_TIME_IN_SECONDS,TimeUnit.SECONDS);


            //存储 refresh_token 到redis
            String encryptedToken = HashingUtils.md5Hash(String.valueOf(accessToken.getRefreshToken())); // 加密Refresh Token
            redisTemplate.opsForValue().set(SecurityConstants.REFRESH_TOKEN_PREFIX + jsonObject.get("username"), encryptedToken, 7, TimeUnit.DAYS);


            if ("member".equals(jsonObject.getString("client_id"))){
                //前台会员登录成功 记录登录日志
                loginLogMessageSender.recordLoginLog(
                        jsonObject.getLong("userId"),
                        userName,
                        LocalDateTime.now(),
                        ip,
                        UserAgentUtil.getDeviceType(request.getHeader("user-agent")),
                        request.getHeader("user-agent"),
                        AuthenticationModeEnum.MEMBER_LOGIN.getCode(),
                        jsonObject.getString("memberType"),
                        jsonObject.getString("firstLoginIp")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestResult.relogin(ResultCode.RELOGIN);
        }

        if(principal.getName().equals("uu")){
            return RestResult.ok(resultObj);
        }else if(principal.getName().equals("merchant")){
            return RestResult.ok(resultObj);
        }else {
            return RestResult.ok(accessToken);
        }


    }

    private Long expireTimeOfTime(ClientEnums clientEnums) {
        long expireTime = -1L;
        if (Objects.nonNull(clientEnums)) {
            Object expireTimeInRedis = redisUtils.hget(RedisConstants.TOKEN_EXPIRE, clientEnums.getName());
            if (Objects.nonNull(expireTimeInRedis)) {
                expireTime = (long) expireTimeInRedis;
            } else {
                RestResult<OAuth2ClientDTO> responseVO = this.oAuthClientFeignClient.getOAuth2ClientById(clientEnums.getName());
                if (RestResult.ok().getCode().equals(responseVO.getCode())) {
                    OAuth2ClientDTO clientDTO = responseVO.getData();
                    expireTime = Objects.isNull(clientDTO) ? -1L : clientDTO.getAccessTokenValidity();
                }
            }
        }
        return expireTime;
    }

    @GetMapping("/public-key")
    public Map<String, Object> getPublicKey() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAKey key = new RSAKey.Builder(publicKey).build();
        return new JWKSet(key).toJSONObject();
    }


    @DeleteMapping("/logout")
    public RestResult logout(HttpServletRequest request) {
        String payload = TokenUtils.getJwtPayload();
        // String payload = request.getHeader(SecurityConstants.JWT_PAYLOAD_KEY);
        JSONObject jsonObject = JSONObject.parseObject(payload);
        String clientId = jsonObject.getString("client_id");

        String jti = jsonObject.getString("jti"); // JWT唯一标识
        long exp = jsonObject.getLong("exp"); // JWT过期时间戳(单位:秒)
        String userName = (String) jsonObject.get("username");
        log.info("登出userName:->{}", userName);
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        if (ClientEnums.UU.equals(ClientEnums.buildClientEnumByClientName(clientId))) {
            String antName = jsonObject.getString("username");
            String registerInviteCode = jsonObject.getString("registerInviteCode");
            CommonUtils.removeOnlineAnt(String.format("%s-%s", RedisConstants.ONLINE_ANT, registerInviteCode), antName, redisUtils);
        } else {
            CommonUtils.deleteToken(userName, redisUtils);
        }
        redisTemplate.opsForValue().set(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti, null, (exp - currentTimeSeconds), TimeUnit.SECONDS);
        return RestResult.ok();
    }

    @GetMapping("/generateToken")
    public String generateToken(String data, HttpServletRequest request) {
        return customTokenService.generateTokenWithoutPasswordCheck(data, request);
    }

    //钱包注册 自动登录
    @GetMapping("/generateTokenForWallet")
    public String generateTokenForWallet(String data, HttpServletRequest request) {
        return customTokenService.generateTokenForWallet(data, request);
    }

    @GetMapping("/generateAppToken")
    public String generateAppToken(String data, HttpServletRequest request) {
        return customTokenService.generateAppTokenWithoutPasswordCheck(data, request);
    }

    /**
     * 后台登录白名单验证
     */
    private boolean backgroundLoginWhitelistVerification(String ip, Principal principal){
        // 后台登录白名单验证
        String clientCode = principal.getName();
        String type = ClientTypeEnum.getTypeByClientCode(clientCode);
        if(!ObjectUtils.isEmpty(type) && type.equals(ClientTypeEnum.MEMBER.getType())){
            return true;
        }
        Map<String, String> params = new HashMap<>();
        params.put("ip", ip);
        params.put("type", type);
        return userFeignClient.getIp(params);
    }
}
