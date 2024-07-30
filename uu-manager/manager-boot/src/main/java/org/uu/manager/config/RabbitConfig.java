package org.uu.manager.config;

import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.manager.consumer.MsgConfirmCallback;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        //设置消息确认回调
        //每当消息被 RabbitMQ 代理确认（无论成功还是失败），都会调用 MsgConfirmCallback 的 confirm 方法
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConfirmCallback(new MsgConfirmCallback());
        return rabbitTemplate;
    }


    //统计会员在线人数队列
    @Bean
    public Queue onlineCountQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_ONLINE_COUNT_QUEUE, true);
    }


    //统计会员在线人数交换机
    @Bean
    public DirectExchange onlineCountExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_ONLINE_COUNT_EXCHANGE);
    }

    //统计会员在线人数路由键
    @Bean
    public Binding bindingOnlineCountQueue(Queue onlineCountQueue, DirectExchange onlineCountExchange) {
        return BindingBuilder.bind(onlineCountQueue).to(onlineCountExchange).with(RabbitMqConstants.WALLET_MEMBER_ONLINE_COUNT_ROUTINGKEY);
    }
}
