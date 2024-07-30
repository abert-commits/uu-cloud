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
import org.uu.wallet.service.TrxAutoCreditService;


/**
 * TRX自动上分
 *
 * @author Simon
 * @date 2024/06/13
 */
@Service
public class TrxAutoCreditConsumer {

    //TRX自动上分 服务
    private final TrxAutoCreditService trxAutoCreditService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    public TrxAutoCreditConsumer(
            TrxAutoCreditService trxAutoCreditService
    ) {
        this.trxAutoCreditService = trxAutoCreditService;
    }

    @RabbitListener(queues = RabbitMqConstants.UU_WALLET_TRX_AUTO_CREDIT_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());

        TaskInfo taskInfo = JSON.parseObject(messageBody, TaskInfo.class);

        try {
            //TRX自动上分
            logger.info("Rabbit 消费队列消息: TRX自动上分: {}", taskInfo.getOrderNo());

            if (trxAutoCreditService.trxAutoCredit(taskInfo.getOrderNo())) {
                logger.info("TRX自动上分, 消息消费成功: {}", taskInfo.getOrderNo());
            } else {
                logger.error("TRX自动上分, 消息消费失败: {}", taskInfo.getOrderNo());
                // 抛出异常, 触发重试机制
                throw new RuntimeException();
            }
            //处理成功 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Rabbit 消费 TRX自动上分: e: {}", e.getMessage());
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
