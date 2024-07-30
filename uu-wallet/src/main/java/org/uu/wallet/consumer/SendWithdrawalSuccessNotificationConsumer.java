package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.service.AsyncNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 发送提现成功通知
 *
 * @author Simon
 * @date 2023/12/21
 */
@Service
public class SendWithdrawalSuccessNotificationConsumer {

    //发送提现成功通知 服务
    private final AsyncNotifyService asyncNotifyService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    public SendWithdrawalSuccessNotificationConsumer(
            AsyncNotifyService asyncNotifyService
    ) {
        this.asyncNotifyService = asyncNotifyService;
    }

    @RabbitListener(queues = RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());

        TaskInfo taskInfo = JSON.parseObject(messageBody, TaskInfo.class);

        try {
            //钱包用户卖出匹配超时
            logger.info("Rabbit 消费队列消息: 发送提现成功通知: {}", taskInfo.getOrderNo());

            if (asyncNotifyService.sendWithDrawSuccessCallbackWithRecordRequest(taskInfo.getOrderNo(), "1")) {
                //处理成功 手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                logger.info("发送提现成功通知, 消息消费成功: {}", taskInfo.getOrderNo());
            } else {
                logger.error("发送提现成功通知, 消息消费失败: {}", taskInfo.getOrderNo());
                //处理失败 抛出异常 进行重试消费
                throw new RuntimeException();
            }
        } catch (Exception e) {
            logger.error("Rabbit消费 发送提现成功通知失败: e: {}", e);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
