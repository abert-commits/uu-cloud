package org.uu.auth.security.config;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.auth.comm.enums.ClientEnums;
import org.uu.auth.security.details.MoreAuthenticatedUserDetailsService;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    private final UserDetailsService sysUserDetailsService;
    private final UserDetailsService walletDetailsService;
    private final UserDetailsService memberDetailsService;
    private final UserDetailsService merchantDetailsService;
    private final UserDetailsService appMemberDetailsService;
//    private final UserDetailsService antDetailsService;


    private final String realName = ".";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests().antMatchers("/oauth/**",
                        "/menu/currentUser",
                        "/config/rechargeCancel/index",
                        "/config/withdrawCancel/index",
                        "/payCenter/**",
                        "/menu/**",
                        "/test/**",
                        "/payCenter/**",
                        "/notify/**",
                        "/v2/api-docs",
                        "/swagger-resources/configuration/ui",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/swagger-resources/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/signUp/**",
                        "/apiCenter/**",
                        "/actuator/health",
                        "/websocket/**",
                        "/druid/*",
                        "/ar-pay/notify/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()

                .and()

                .csrf().disable()


                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .headers().httpStrictTransportSecurity().maxAgeInSeconds(0).includeSubDomains(true)
        ;
    }

    /**
     * 认证管理对象
     *
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 添加自定义认证器
     *
     * @param auth
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    /**
     * 设置默认的用户名密码认证授权提供者
     *
     * @return
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        Map<String, UserDetailsService> userDetailsServiceMap = new HashMap<>();
        userDetailsServiceMap.put(ClientEnums.ADMIN_CLIENT.getName(), sysUserDetailsService);
        userDetailsServiceMap.put(ClientEnums.WALLET_CLIENT.getName(), walletDetailsService);
        userDetailsServiceMap.put(ClientEnums.MEMBER_CLIENT.getName(), memberDetailsService);
        userDetailsServiceMap.put(ClientEnums.MERCHANT_CLIENT.getName(), merchantDetailsService);
        userDetailsServiceMap.put(ClientEnums.APP_CLIENT.getName(), appMemberDetailsService);
//        userDetailsServiceMap.put(ClientEnums.UU.getName(), memberDetailsService);
        provider.setUserDetailsService(new MoreAuthenticatedUserDetailsService(userDetailsServiceMap));
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false); // 是否隐藏用户不存在异常，默认:true-隐藏；false-抛出异常；
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 自定义认证异常响应数据
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, e) -> {
            if (e instanceof InsufficientAuthenticationException) {
                response.addHeader("WWW-Authenticate", "Basic realm= " + realName);
                response.sendError(org.springframework.http.HttpStatus.UNAUTHORIZED.value(), org.springframework.http.HttpStatus.UNAUTHORIZED.getReasonPhrase());
            } else {
                response.setStatus(cn.hutool.http.HttpStatus.HTTP_OK);
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Cache-Control", "no-cache");
                RestResult result = RestResult.failure(ResultCode.CLIENT_AUTHENTICATION_FAILED);
                response.getWriter().print(JSONUtil.toJsonStr(result));
                response.getWriter().flush();
            }
        };
    }

}
