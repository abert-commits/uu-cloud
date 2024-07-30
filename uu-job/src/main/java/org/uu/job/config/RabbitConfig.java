//package org.uu.job.config;
//
//import org.uu.manager.consumer.MsgConfirmCallback;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//public class RabbitConfig {
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//
//        //设置消息确认回调
//        //每当消息被 RabbitMQ 代理确认（无论成功还是失败），都会调用 MsgConfirmCallback 的 confirm 方法
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setConfirmCallback(new MsgConfirmCallback());
//        return rabbitTemplate;
//    }
//
//}
