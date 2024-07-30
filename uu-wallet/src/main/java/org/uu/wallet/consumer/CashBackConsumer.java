package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.service.ICashBackOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 订单标记 消费者
 *
 * @author Simon
 * @date 2023/1/13
 */
@Service
public class CashBackConsumer {

    @Resource
    ICashBackOrderService cashBackOrderService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.CASH_BACK_ORDER_PROCESS_DEAD_LETTER_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {

        try {
            String messageJson = new String(message.getBody());
            logger.info("Rabbit 消费队列消息: 用户余额退回: {}", messageJson);
            JSONObject jsonObject = JSON.parseObject(messageJson);
            if (cashBackOrderService != null
                && jsonObject.containsKey("orderNo")
            ) {
                String data = jsonObject.getString("orderNo");
                JSONObject dataObject = JSON.parseObject(data);
                if(dataObject.containsKey("orderNo")){
                    if(!dataObject.containsKey("processUserName")){
                        dataObject.put("processUserName", "admin");
                    }
                    cashBackOrderService.cashBack(dataObject.getString("orderNo"), dataObject.getString("processUserName"));
                    logger.info("Rabbit 消费队列消息: 用户余额退回 消费成功;");
                    //处理成功 手动确认消息
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            } else {
                logger.error("Rabbit 消费队列消息: 用户余额退回 消费失败;");
                // 抛出异常, 触发重试机制
                throw new RuntimeException();
            }
        } catch (Exception e) {
            logger.error("Rabbit 消费队列消息: 用户余额退回 消费失败, 报错: {}", e.getMessage());
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
