package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.service.HandleOrderTimeoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


/**
 * 支付超时消费者 (监听死信队列)
 *
 * @author Simon
 * @date 2023/12/21
 */
@Service
public class PaymentTimeoutConsumer {

    //处理订单超时服务
    private final HandleOrderTimeoutService handleOrderTimeoutService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    public PaymentTimeoutConsumer(
            HandleOrderTimeoutService handleOrderTimeoutService
    ) {
        this.handleOrderTimeoutService = handleOrderTimeoutService;
    }

    @RabbitListener(queues = RabbitMqConstants.UU_WALLET_MEMBER_PAYMENT_DEAD_LETTER_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());

        TaskInfo taskInfo = JSON.parseObject(messageBody, TaskInfo.class);

        try {
            // 根据任务类型调用不同的服务
            switch (taskInfo.getTaskType()) {
                case "5":
                    //支付超时
                    logger.info("Rabbit 消费延时队列消息: 买入订单支付超时: {}", taskInfo.getOrderNo());

                    if (handleOrderTimeoutService.handlePaymentTimeout(taskInfo.getOrderNo())) {
                        //处理成功 手动确认消息
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

                        logger.info("买入订单支付超时, 消息消费成功: {}", taskInfo.getOrderNo());
                    } else {
                        logger.error("买入订单支付超时, 消息消费失败: {}", taskInfo.getOrderNo());
                        //处理失败 抛出异常 进行重试消费
                        throw new RuntimeException();
                    }
                    break;
                case "6":
                    //USDT支付超时
                    logger.info("Rabbit 消费延时队列消息: USDT支付超时: {}", taskInfo.getOrderNo());

                    if (handleOrderTimeoutService.handleUsdtPaymentTimeout(taskInfo.getOrderNo())) {
                        //处理成功 手动确认消息
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

                        logger.info("USDT支付超时, 消息消费成功: {}", taskInfo.getOrderNo());
                    } else {
                        logger.error("USDT支付超时, 消息消费失败: {}", taskInfo.getOrderNo());
                        //处理失败 抛出异常 进行重试消费
                        throw new RuntimeException();
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Rabbit MQ处理延时订单超时任务失败: e: {}", e);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
