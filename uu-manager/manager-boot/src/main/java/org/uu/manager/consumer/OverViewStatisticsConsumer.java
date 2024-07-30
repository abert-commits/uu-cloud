package org.uu.manager.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.manager.service.IBiOverViewStatisticsDailyService;
import org.uu.manager.websocket.SendOnlineMemberCount;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 概览统计 消费者
 *
 * @author lukas
 */
@Service
public class OverViewStatisticsConsumer {

    @Resource
    private IBiOverViewStatisticsDailyService biOverViewStatisticsDailyService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

//    @RabbitListener(bindings = @QueueBinding(value = @Queue(), exchange = @Exchange(value = "over.view.statistics.queue", type = ExchangeTypes.FANOUT)))
    @RabbitListener(queues = RabbitMqConstants.OVER_VIEW_STATISTICS_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());
        OrderEventReq req = JSON.parseObject(messageBody, OrderEventReq.class);
        try {
            if(biOverViewStatisticsDailyService.statisticsDaily(req)){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            logger.error("Rabbit 消费队列消息: 概览统计 消费失败, e: {}, req:{}", e.getMessage(), req);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }


    /**
     * RabbitMq 多消费者模式 同一个队列
     * 多个消费者同时进行消费 只绑定交换机、不绑定队列。
     * @RabbitListener(bindings = @QueueBinding(value = @Queue(), exchange = @Exchange(value = "over.view.statistics.queue", type = ExchangeTypes.TOPIC)))
     * @param message
     * @param channel
     */
//    @RabbitListener(queues = RabbitMqConstants.MERCHANT_DAILY_STATISTICS_QUEUE, concurrency = "5-10")
    public void multiQueueConsumption(Message message, Channel channel) {
        String messageBody = new String(message.getBody());
        OrderEventReq req = JSON.parseObject(messageBody, OrderEventReq.class);
        try {
            biOverViewStatisticsDailyService.statisticsMerchantDaily(req);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Rabbit 消费队列消息: 概览统计 消费失败, e: {}, req:{}", e.getMessage(), req);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
