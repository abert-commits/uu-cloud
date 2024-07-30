package org.uu.job.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
