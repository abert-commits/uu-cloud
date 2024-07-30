package org.uu.wallet.webSocket;

import org.jetbrains.annotations.NotNull;
import org.uu.wallet.service.OrderChangeEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class WebSocketServiceInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberAmountListWebSocketService memberAmountListWebSocketService;

    @Autowired
    private MemberMessageWebSocketService notifyOrderStatusChangeWebSocketService;

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        memberAmountListWebSocketService.setRedisTemplate(redisTemplate);
        notifyOrderStatusChangeWebSocketService.setRedisTemplate(redisTemplate);
    }
}
