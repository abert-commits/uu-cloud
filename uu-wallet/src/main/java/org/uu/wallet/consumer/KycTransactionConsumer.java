package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.KycTransactionMessage;
import org.uu.wallet.service.impl.IKycCenterServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 获取KYC交易记录 消费者
 *
 * @author Simon
 * @date 2023/1/13
 */
@Service
public class KycTransactionConsumer {

    @Autowired
    private IKycCenterServiceImpl kycCenterService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.WALLET_MEMBER_KYC_TRANSACTION_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {

        logger.info("Rabbit 消费队列消息: 通过KYC验证完成订单");

        String messageBody = new String(message.getBody());

        KycTransactionMessage kycTransactionMessage = null;
        try {
            //获取消息信息
            kycTransactionMessage = JSON.parseObject(messageBody, KycTransactionMessage.class);

            if (kycTransactionMessage == null) {
                // 处理失败了 也要把消息消费掉
                logger.error("Rabbit 消费队列消息: 通过KYC验证完成订单 获取MQ消息失败 手动确认消息");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            try {
                // 获取消息信息失败了 要把消息消费掉
                logger.error("Rabbit 消费队列消息: 通过KYC验证完成订单 获取MQ消息失败 手动确认消息");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }


        try {

            // 通过 KYC 验证完成订单
//            Boolean finalizeOrderWithKYCVerification = kycCenterService.finalizeOrderWithKYCVerification(kycTransactionMessage);

//            logger.info("Rabbit 消费队列消息: 通过KYC验证完成订单 消费成功, finalizeOrderWithKYCVerification: {}", finalizeOrderWithKYCVerification);
            //处理成功 手动确认消息
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            try {
                // 处理失败了 也要把消息消费掉
                logger.error("Rabbit 消费队列消息: 通过KYC验证完成订单 处理失败 手动确认消息");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
