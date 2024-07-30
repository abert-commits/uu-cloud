package org.uu.wallet.consumer;

import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.service.IClearDailyTransactionDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 清空每日交易数据 消费者
 *
 * @author Simon
 * @date 2023/1/13
 */
@Service
public class ClearDailyTransactionDataConsumer {

    @Autowired
    private IClearDailyTransactionDataService clearDailyTransactionDataService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.WALLET_MEMBER_DAILY_TRADE_CLEUU_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {

        try {
            //清空每日交易数据
            logger.info("Rabbit 消费队列消息: 清空每日交易数据");

            if (clearDailyTransactionDataService != null) {
                if (clearDailyTransactionDataService.clearDailyTradeData()) {
                    logger.info("Rabbit 消费队列消息: 清空每日交易数据 消费成功");
                    //处理成功 手动确认消息
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                } else {
                    logger.error("Rabbit 消费队列消息: 清空每日交易数据 消费失败, 执行sql失败");
                    // 抛出异常, 触发重试机制
                    throw new RuntimeException();
                }
            } else {
                logger.error("Rabbit 消费队列消息: 清空每日交易数据 消费失败, sendOnlineMemberCount 为null");
                // 抛出异常, 触发重试机制
                throw new RuntimeException();
            }
        } catch (Exception e) {
            logger.error("Rabbit 消费队列消息: 清空每日交易数据 消费失败, e: {}", e);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
