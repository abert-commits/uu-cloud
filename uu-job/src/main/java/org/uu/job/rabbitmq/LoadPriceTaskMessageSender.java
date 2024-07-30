package org.uu.job.rabbitmq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.job.entity.CustomCorrelationData2;
import org.uu.wallet.entity.TaskInfo;

import java.util.UUID;

@Service
@Slf4j
public class LoadPriceTaskMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送 获取实时汇率 MQ
     *
     * @param taskInfo
     */
    public void sendLoadPriceMessage(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_REALTIME_EXCHANGE_RATE_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData2 correlationData = new CustomCorrelationData2(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 获取实时汇率 消息内容: {}, message: {}", message, message);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }
}
