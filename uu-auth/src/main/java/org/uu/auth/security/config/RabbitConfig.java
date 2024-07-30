package org.uu.auth.security.config;

import org.uu.common.core.constant.RabbitMqConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {
    public final static Logger logger = LoggerFactory.getLogger(RabbitConfig.class);

    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        //设置消息确认回调
        //每当消息被 RabbitMQ 代理确认（无论成功还是失败），都会调用 MsgConfirmCallback 的 confirm 方法
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConfirmCallback(new MsgConfirmCallback());
        return rabbitTemplate;
    }

    //会员登录日志记录队列
    @Bean
    public Queue loginLogQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_LOGIN_LOG_QUEUE, true);
    }


    //会员登录日志记录交换机
    @Bean
    public DirectExchange loginLogExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_LOGIN_LOG_EXCHANGE);
    }

    //会员登录日志记录路由键
    @Bean
    public Binding bindingLoginLogQueue(Queue loginLogQueue, DirectExchange loginLogExchange) {
        return BindingBuilder.bind(loginLogQueue).to(loginLogExchange).with(RabbitMqConstants.WALLET_MEMBER_ROUTING_KEY_LOGIN_LOG);
    }
}
