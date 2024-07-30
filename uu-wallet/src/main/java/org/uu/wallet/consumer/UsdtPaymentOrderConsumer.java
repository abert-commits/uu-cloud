package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.service.UsdtPaymentOrderService;


/**
 * 处理USDT代付订单
 *
 * @author Simon
 * @date 2024/06/13
 */
@Service
public class UsdtPaymentOrderConsumer {

    //处理USDT代付订单 服务
    private final UsdtPaymentOrderService usdtPaymentOrderService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    public UsdtPaymentOrderConsumer(
            UsdtPaymentOrderService usdtPaymentOrderService
    ) {
        this.usdtPaymentOrderService = usdtPaymentOrderService;
    }

    @RabbitListener(queues = RabbitMqConstants.UU_WALLET_USDT_PAYMENT_ORDER_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());

        TaskInfo taskInfo = JSON.parseObject(messageBody, TaskInfo.class);

        try {
            //处理USDT代付订单
            logger.info("Rabbit 消费队列消息: 处理USDT代付订单: {}", taskInfo.getOrderNo());

            if (usdtPaymentOrderService.usdtPaymentOrder(taskInfo.getOrderNo())) {
                logger.info("处理USDT代付订单, 消息消费成功: {}", taskInfo.getOrderNo());
            } else {
                logger.error("处理USDT代付订单, 消息消费失败: {}", taskInfo.getOrderNo());
            }
            //处理成功 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Rabbit 消费 处理USDT代付订单失败: e: {}", e.getMessage());
            //消费失败不进行重试
            try {
                //消费失败了 也手动确认消息 不进行重试
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e1) {
                logger.error("Rabbit 消费 处理USDT代付订单, 手动确认消息失败 : e: {}", e.getMessage());
            }
        }
    }
}
