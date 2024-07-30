package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.service.IMerchantCollectOrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


/**
 * 代收订单支付超时处理 (监听死信队列)
 *
 * @author Simon
 * @date 2024/03/08
 */
@Service
@RequiredArgsConstructor
public class MerchantCollectOrderPaymentTimeoutConsumer {

    //语音通知会员
    private final IMerchantCollectOrdersService merchantCollectOrdersService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());

        TaskInfo taskInfo = JSON.parseObject(messageBody, TaskInfo.class);

        try {
            //代收订单支付超时处理
            logger.info("Rabbit 消费延时队列消息: 代收订单支付超时: {}", taskInfo.getOrderNo());

            if (merchantCollectOrdersService.handlePaymentTimeout(taskInfo.getOrderNo())) {
                //处理成功 手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

                logger.info("代收订单支付超时处理成功, 消息消费成功: {}", taskInfo.getOrderNo());
            } else {
                logger.error("代收订单支付超时处理失败, 消息消费失败: {}", taskInfo.getOrderNo());
                //处理失败 抛出异常 进行重试消费
                throw new RuntimeException();
            }
        } catch (Exception e) {
            logger.error("代收订单支付超时处理失败 Rabbit MQ处理延时订单超时任务失败: e: {}", e);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
