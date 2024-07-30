package org.uu.wallet.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.tron.service.TronBlockService;


/**
 * USDT资金归集-消费者
 *
 * @author Simon
 * @date 2024/07/12
 */
@Service
public class UsdtLoadBalanceConsumer {

    //USDT资金归集 服务
    private final TronBlockService tronBlockService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    public UsdtLoadBalanceConsumer(
            TronBlockService tronBlockService
    ) {
        this.tronBlockService = tronBlockService;
    }

    @RabbitListener(queues = RabbitMqConstants.UU_WALLET_USDT_COLLECTION_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {

        try {
            logger.info("Rabbit 消费队列消息: USDT资金归集");

            //资金归集
            tronBlockService.loadBalance();

            logger.info("USDT资金归集, 消息消费成功");
            //处理成功 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Rabbit 消费 USDT资金归集失败 : e: {}", e.getMessage());
            try {
                //消费失败了 也手动确认消息 不进行重试
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e1) {
                logger.error("Rabbit 消费 USDT资金归集失败, 手动确认消息失败 : e: {}", e.getMessage());
            }
        }
    }
}
