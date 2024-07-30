package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.common.core.message.KycCompleteMessage;
import org.uu.wallet.service.impl.IKycCenterServiceImpl;

import javax.annotation.Resource;


/**
 * @author lukas
 */
@Service
public class KycAutoCompleteConsumer {

    @Resource
    private IKycCenterServiceImpl kycCenterService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.KYC_AUTO_COMPLETE_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());
        KycCompleteMessage kycCompleteMessage = JSON.parseObject(messageBody, KycCompleteMessage.class);
        try {
            logger.info("Rabbit 消费队列消息: 自动完成kyc交易账变处理: {}", kycCompleteMessage);
            if(kycCenterService.transactionSuccessHandler(kycCompleteMessage)){
                //处理成功 手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            logger.error("Rabbit 消费 自动完成kyc交易账变处理: e: {}", e.getMessage());
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
