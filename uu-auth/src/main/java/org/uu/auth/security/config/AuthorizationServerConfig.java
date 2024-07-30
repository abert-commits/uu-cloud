package org.uu.auth.security.config;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.uu.auth.security.details.client.ClientDetailsServiceImpl;
import org.uu.auth.security.details.member.MemberDetails;
import org.uu.auth.security.details.sixNine.AntDetails;
import org.uu.auth.security.details.user.SysUserDetails;
import org.uu.common.core.constant.SecurityConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;
    private final ClientDetailsServiceImpl clientDetailsService;
    private final RedisTemplate redisTemplate;

    /**
     * OAuth2客户端
     */
    @Override
    @SneakyThrows
    public void configure(ClientDetailsServiceConfigurer clients) {
        clients.withClientDetails(clientDetailsService);
    }

    /**
     * 配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        // Token增强
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> tokenEnhancers = new ArrayList<>();
        tokenEnhancers.add(tokenEnhancer());
        tokenEnhancers.add(jwtAccessTokenConverter());
        tokenEnhancerChain.setTokenEnhancers(tokenEnhancers);

        // 获取原有默认授权模式(授权码模式、密码模式、客户端模式、简化模式)的授权者
        List<TokenGranter> granterList = new ArrayList<>(Arrays.asList(endpoints.getTokenGranter()));

        CompositeTokenGranter compositeTokenGranter = new CompositeTokenGranter(granterList);
        endpoints
                .authenticationManager(authenticationManager)
                .accessTokenConverter(jwtAccessTokenConverter())
                .tokenEnhancer(tokenEnhancerChain)
                .tokenGranter(compositeTokenGranter)
                .reuseRefreshTokens(true)
                .tokenServices(tokenServices(endpoints))
        ;
    }

    public DefaultTokenServices tokenServices(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> tokenEnhancers = new ArrayList<>();
        tokenEnhancers.add(tokenEnhancer());
        tokenEnhancers.add(jwtAccessTokenConverter());
        tokenEnhancerChain.setTokenEnhancers(tokenEnhancers);

        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(endpoints.getTokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setTokenEnhancer(tokenEnhancerChain);
        return tokenServices;

    }

    /**
     * JWT内容增强
     */
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return (accessToken, authentication) -> {
            Map<String, Object> additionalInfo = CollectionUtil.newHashMap();
            Object principal = authentication.getUserAuthentication().getPrincipal();

            // 获取客户端ID
            String clientId = authentication.getOAuth2Request().getClientId();

            // 定义Redis中存储增强内容的键的前缀，包括客户端ID
            final String REDIS_ENHANCER_KEY_PREFIX  = SecurityConstants.REDIS_ENHANCER_KEY_PREFIX + clientId + ":";

            if (principal instanceof SysUserDetails){
                SysUserDetails sysUserDetails = (SysUserDetails) principal;
                additionalInfo.put("userId", sysUserDetails.getUserId());
                additionalInfo.put("username", sysUserDetails.getUsername());

                //将增强内容存储到redis
                // 使用FastJson序列化增强内容为字符串
                String additionalInfoStr = JSON.toJSONString(additionalInfo);

                // 存储增强内容到Redis，包括客户端信息
                redisTemplate.opsForValue().set(REDIS_ENHANCER_KEY_PREFIX + sysUserDetails.getUsername(), additionalInfoStr);

                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
            }

            if (principal instanceof AntDetails) {
                AntDetails antDetails = (AntDetails) principal;
                additionalInfo.put("antId", antDetails.getAnyId());
                additionalInfo.put("username", antDetails.getUsername());
                additionalInfo.put("registerInviteId", antDetails.getRegisterInviteId());
                additionalInfo.put("registerInviteCode", antDetails.getRegisterInviteCode());
                //将增强内容存储到redis
                // 使用FastJson序列化增强内容为字符串
                String additionalInfoStr = JSON.toJSONString(additionalInfo);

                // 存储增强内容到Redis，包括客户端信息
                redisTemplate.opsForValue().set(REDIS_ENHANCER_KEY_PREFIX + antDetails.getUsername(), additionalInfoStr);
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
            }

            if (principal instanceof MemberDetails){
                MemberDetails sysUserDetails = (MemberDetails) principal;
                additionalInfo.put("userId", sysUserDetails.getUserId());
                additionalInfo.put("username", sysUserDetails.getUsername());
                additionalInfo.put("memberId", sysUserDetails.getUserId());
                additionalInfo.put("memberType", sysUserDetails.getMemberType());
                additionalInfo.put("firstLoginIp", sysUserDetails.getFirstLoginIp());
                additionalInfo.put("antLevel", sysUserDetails.getAntLevel());
                additionalInfo.put("areaCode", "+91");
                additionalInfo.put("buyRewardRatio", sysUserDetails.getBuyRewardRatio());

                //将增强内容存储到redis
                // 使用FastJson序列化增强内容为字符串
                String additionalInfoStr = JSON.toJSONString(additionalInfo);

                // 存储增强内容到Redis，包括客户端信息
                redisTemplate.opsForValue().set(REDIS_ENHANCER_KEY_PREFIX + sysUserDetails.getUsername(), additionalInfoStr);
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
            }

            //刷新令牌 增强JWT
            if (principal instanceof String){

                //刷新令牌的principal就是username
                String redisKey = REDIS_ENHANCER_KEY_PREFIX + principal;

                // 检查键是否存在
                if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                    // 从Redis获取增强内容，确保操作的类型正确
                    String additionalInfoStr = (String) redisTemplate.opsForValue().get(redisKey);
                    if (StringUtils.isNotEmpty(additionalInfoStr)) { // 检查获取到的数据是否为null
                        Map<String, Object> fromRedis = JSON.parseObject(additionalInfoStr, Map.class);
                        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(fromRedis);
                    }else{
                        throw new InvalidTokenException("Token illegal or invalid, please re-login");
                    }
                }else{
                    throw new InvalidTokenException("Token illegal or invalid, please re-login");
                }

            }
            return accessToken;
        };
    }

    /**
     * 使用非对称加密算法对token签名
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyPair());
        return converter;
    }

    /**
     * 密钥库中获取密钥对(公钥+私钥)
     */
    @Bean
    public KeyPair keyPair() {
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "123456".toCharArray());
        KeyPair keyPair = factory.getKeyPair("jwt", "123456".toCharArray());
        return keyPair;
    }


}
