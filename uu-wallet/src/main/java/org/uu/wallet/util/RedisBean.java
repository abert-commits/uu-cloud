package org.uu.wallet.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 这个类 RedisBean 是一个 Spring 组件，用于将 RedisTemplate 实例静态化，使其可以在非 Spring 管理的类中被访问和使用。
 * webSocket有时候会获取不到RedisTemplate 所以用这个类来获取
 * @author Simon
 * @date 2023/12/15
 */
@Component
@Slf4j
public class RedisBean {

    public static RedisTemplate redis;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void getRedisTemplate() {
        redis = this.redisTemplate;
    }

}
