package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.common.core.message.KycCompleteMessage;
import org.uu.common.core.result.KycRestResult;
import org.uu.wallet.req.KycAutoCompleteReq;
import org.uu.wallet.service.impl.IKycCenterServiceImpl;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * @author lukas
 */
@Service
public class KycWithdrawCompleteConsumer {

    @Resource
    private IKycCenterServiceImpl kycCenterService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageJson = new String(message.getBody());
        logger.info("Rabbit 消费队列消息: kyc提现自动完成: {}", messageJson);
        JSONObject jsonObject = JSON.parseObject(messageJson);
        try {
            if (jsonObject.containsKey("orderNo")) {
                String data = jsonObject.getString("orderNo");
                KycAutoCompleteReq kycCompleteMessage = JSON.parseObject(data, KycAutoCompleteReq.class);
                logger.info("Rabbit 消费队列消息: kyc提现自动完成交易账变处理: {}", kycCompleteMessage);
                KycRestResult kycRestResult = kycCenterService.completePayment(kycCompleteMessage);
                if(Objects.equals(kycRestResult.getCode(), "1")){
                    //处理成功 手动确认消息
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            }
        } catch (Exception e) {
            logger.error("Rabbit 消费 kyc提现自动完成交易账变处理: e: {}", e.getMessage());
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
