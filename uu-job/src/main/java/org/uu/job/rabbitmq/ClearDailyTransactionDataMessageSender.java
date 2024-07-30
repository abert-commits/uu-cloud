package org.uu.job.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.job.entity.CustomCorrelationData;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ClearDailyTransactionDataMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送 清空会员每日交易数据的MQ
     *
     * @param clearDailyTransactionDataMessage
     */
    public void sendClearDailyTransactionDataMessage(String clearDailyTransactionDataMessage) {

        String queueName = RabbitMqConstants.WALLET_MEMBER_DAILY_TRADE_CLEUU_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                queueName
        );

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(clearDailyTransactionDataMessage.getBytes(), messageProperties);

        log.info("RabbitMQ发送 清空会员每日交易数据的MQ 消息内容: {}, message: {}", message, clearDailyTransactionDataMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }
}
