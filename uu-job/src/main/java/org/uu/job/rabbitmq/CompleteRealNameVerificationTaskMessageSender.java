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
public class CompleteRealNameVerificationTaskMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送 完成实名认证任务的MQ
     *
     * @param completeRealNameVerificationTaskMessage
     */
    public void sendClearDailyTransactionDataMessage(String completeRealNameVerificationTaskMessage) {

        String queueName = RabbitMqConstants.WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_QUEUE;

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
        Message message = new Message(completeRealNameVerificationTaskMessage.getBytes(), messageProperties);

        log.info("RabbitMQ发送 完成实名认证任务的MQ 消息内容: {}, message: {}", message, completeRealNameVerificationTaskMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }
}
