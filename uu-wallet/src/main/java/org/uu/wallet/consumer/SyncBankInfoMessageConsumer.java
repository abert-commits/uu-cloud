package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.service.ICollectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


/**
 * 同步银行卡信息
 *
 * @author Simon
 * @date 2024/06/13
 */
@Service
public class SyncBankInfoMessageConsumer {

    //同步银行卡信息 服务
    private final ICollectionInfoService collectionInfoService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    public SyncBankInfoMessageConsumer(
            ICollectionInfoService collectionInfoService
    ) {
        this.collectionInfoService = collectionInfoService;
    }

    @RabbitListener(queues = RabbitMqConstants.UU_WALLET_SYNC_BANK_INFO_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());

        TaskInfo taskInfo = JSON.parseObject(messageBody, TaskInfo.class);

        try {
            //同步银行卡信息
            logger.info("Rabbit 消费队列消息: 同步银行卡信息: {}", taskInfo.getOrderNo());

            if (collectionInfoService.syncBankInfo(taskInfo.getOrderNo())) {
                logger.info("同步银行卡信息, 消息消费成功: {}", taskInfo.getOrderNo());
            } else {
                logger.error("同步银行卡信息, 消息消费失败: {}", taskInfo.getOrderNo());
                // 抛出异常, 触发重试机制
                throw new RuntimeException();
            }
            //处理成功 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Rabbit 消费 同步银行卡信息失败: e: {}", e.getMessage());
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
