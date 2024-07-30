package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.TradIpBlackMessage;
import org.uu.wallet.service.ITradeIpBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 添加交易IP黑名单 消费者
 *
 * @author Simon
 * @date 2023/1/13
 */
@Service
public class TradeIpBlackAddConsumer {

    @Autowired
    private ITradeIpBlacklistService tradeIpBlacklistService;


    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.WALLET_TRADE_IP_BLACK_ADD_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {

        try {
            String messageBody = new String(message.getBody());
            logger.info("Rabbit 消费队列消息: 添加交易IP黑名单, 收到的消息: {}", messageBody);
            TradIpBlackMessage tradIpBlackMessage = JSON.parseObject(messageBody, TradIpBlackMessage.class);
            tradeIpBlacklistService.addBlackIpCallback(tradIpBlackMessage);
            logger.info("Rabbit 消费队列消息: 添加交易IP黑名单 消费成功");
            //处理成功 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Rabbit 消费队列消息: 添加交易IP黑名单 消费失败, e: ", e);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
