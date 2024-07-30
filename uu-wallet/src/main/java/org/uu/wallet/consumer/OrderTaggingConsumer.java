package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.Enum.OrderTypeEnum;
import org.uu.wallet.entity.OrderTaggingMessage;
import org.uu.wallet.service.ICollectionOrderService;
import org.uu.wallet.service.IMatchingOrderService;
import org.uu.wallet.service.IPaymentOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 订单标记 消费者
 *
 * @author Simon
 * @date 2023/1/13
 */
@Service
public class OrderTaggingConsumer {

    @Autowired
    private ICollectionOrderService collectionOrderService;
    @Autowired
    private IPaymentOrderService paymentOrderService;
    @Autowired
    private IMatchingOrderService matchingOrderService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.WALLET_ORDER_TAGGING_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {

        try {
            String messageBody = new String(message.getBody());
            logger.info("Rabbit 消费队列消息: 订单标记, 收到的消息: {}", messageBody);
            OrderTaggingMessage orderTaggingMessage = JSON.parseObject(messageBody, OrderTaggingMessage.class);
            int orderSize = orderTaggingMessage.getPlatformOrderTags() == null ? 0 : orderTaggingMessage.getPlatformOrderTags().size();
            if (OrderTypeEnum.COLLECTION.getCode().equals(orderTaggingMessage.getOrderType())) {
                logger.info("Rabbit 消费队列消息: 订单标记, 处理买入订单, 订单数:{}", orderSize);
                collectionOrderService.taggingOrders(orderTaggingMessage.getRiskType(), new ArrayList<>(orderTaggingMessage.getPlatformOrderTags().keySet()));

            } else if (OrderTypeEnum.PAYMENT.getCode().equals(orderTaggingMessage.getOrderType())) {
                logger.info("Rabbit 消费队列消息: 订单标记, 处理卖出订单, 订单数:{}", orderSize);
                paymentOrderService.taggingOrders(orderTaggingMessage.getRiskType(), new ArrayList<>(orderTaggingMessage.getPlatformOrderTags().keySet()));

            } else if (OrderTypeEnum.MATCH.getCode().equals(orderTaggingMessage.getOrderType())) {
                logger.info("Rabbit 消费队列消息: 订单标记, 处理撮合订单, 订单数:{}", orderSize);
                matchingOrderService.taggingOrders(orderTaggingMessage.getRiskType(), orderTaggingMessage.getPlatformOrderTags());
            }
            logger.info("Rabbit 消费队列消息: 订单标记 消费成功");
            //处理成功 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            logger.error("Rabbit 消费队列消息: 订单标记 消费失败, e: ", e);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
