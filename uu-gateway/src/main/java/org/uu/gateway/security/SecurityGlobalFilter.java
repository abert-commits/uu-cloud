package org.uu.gateway.security;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.nimbusds.jose.JWSObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.SecurityConstants;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.redis.util.RedisUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityGlobalFilter implements GlobalFilter, Ordered {
    private final RedisTemplate redisTemplate;
    private final RedisUtils redisUtils;

    //优化 Redis 的负载 1分钟才更新一次用户的最后活动时间
    private static final long UPDATE_INTERVAL_IN_SECONDS = 60; // 1分钟
    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 不是正确的的JWT不做解析处理
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_KEY);
        if (StrUtil.isBlank(token) || !StrUtil.startWithIgnoreCase(token, SecurityConstants.JWT_PREFIX)) {
            return chain.filter(exchange);
        }

        // 解析JWT获取jti，以jti为key判断redis的黑名单列表是否存在，存在则拦截访问
        token = StrUtil.replaceIgnoreCase(token, SecurityConstants.JWT_PREFIX, Strings.EMPTY);
        String payload = StrUtil.toString(JWSObject.parse(token).getPayload());
        log.info("SecurityGlobalFilter->{}", payload);
        JSONObject jsonObject = JSONObject.parseObject(payload);
        String clientId = jsonObject.getString("client_id");
        String jti = jsonObject.getString("jti"); // JWT唯一标识

        // JWT唯一标识
        String username = jsonObject.get("username") != null ? jsonObject.getString("username") : jsonObject.getString("user_name");

        long exp = jsonObject.getLong("exp"); // JWT过期时间戳(单位:秒)
        Boolean isBlack = redisTemplate.hasKey(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti);
        //校验是否存在黑名单
        if (isBlack) return onFailure(exchange.getResponse(), "User disabled");

        //校验jti是否存在并且是否正确
        String gjti = (String) redisTemplate.opsForValue().get(SecurityConstants.LOGIN_USER_ID + username);
        if (!clientId.equals("merchant")) {
            if(!jti.equals(gjti)){
                return onFailure(exchange.getResponse(), "Token illegal or invalid, please re-login");
            }
        }




        //获取当前时间戳(秒)
        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        boolean isTokenExpired = exp < currentTimeSeconds;

        // token的exp过期了
        if (isTokenExpired) {
            // Token过期
            return onFailure(exchange.getResponse(), "Token illegal or invalid, please re-login");
        }

        if (StringUtils.isNotEmpty(username)){
            if(redisUtils.hHasKey(GlobalConstants.ONLINE_USER_KEY, username)){
                CommonUtils.insertToken(username, redisUtils, GlobalConstants.timeOut);
            }
        }


        //获取用户最后活动时间
        Object value = redisTemplate.opsForValue().get(SecurityConstants.LAST_ACTIVITY_TIME_PREFIX + username);
        Long lastActivityTime = null;

        if (value instanceof Number) {
            lastActivityTime = ((Number) value).longValue();
        }

        // 用户最后活动时间距离现在超过了1分钟那么才进行更新这个值 (优化redis的负载 最快1分钟才更新一次redis用户的最后活动时间)
        if (lastActivityTime == null || (currentTimeSeconds - lastActivityTime > UPDATE_INTERVAL_IN_SECONDS)) {

            //判断请求方法不是 获取在线用户人数 才更新Redis中最后活动时间
            if (!request.getURI().getPath().equals("/uu-manager/api/v1/users/online")) {
                // 更新Redis中的最后活动时间
                redisTemplate.opsForValue().set(SecurityConstants.LAST_ACTIVITY_TIME_PREFIX + username, currentTimeSeconds);
            }
        }


        request = exchange.getRequest().mutate()
                .header(SecurityConstants.JWT_PAYLOAD_KEY, URLEncoder.encode(payload, "UTF-8"))
                .build();
        exchange = exchange.mutate().request(request).build();
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public Mono<Void> onFailure(ServerHttpResponse response, String msg) {

        //byte[] msbit = RestResult.relogin(ResultCode.RELOGIN).toString().getBytes(StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "401");
        jsonObject.put("data", null);
        jsonObject.put("msg", msg);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = response.bufferFactory().wrap(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

}
