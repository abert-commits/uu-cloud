package org.uu.wallet.consumer;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.common.core.message.CommissionAndDividendsMessage;
import org.uu.common.core.message.ExecuteCommissionAndDividendsMessage;
import org.uu.wallet.service.ExecuteCommissionOrDividendsService;

@Slf4j
@Service
@SuppressWarnings("all")
@RequiredArgsConstructor
public class ExecuteCommissionOrDividendsConsumer {
    private final ExecuteCommissionOrDividendsService executeCommissionOrDividendsService;

    @RabbitListener(queues = RabbitMqConstants.EXECUTE_COMMISSION_AND_DIVIDENDS_QUEUE, concurrency = "5-10")
    public void executeCommissionAndDividends(Message message, Channel channel) {
        try {
            String messageStr = new String(message.getBody());
            log.info("[Rabbit消费执行分红和返佣队列消息] 消息内容: {}", messageStr);
            ExecuteCommissionAndDividendsMessage executeCommissionAndDividendsMessage = JSONObject.parseObject(
                    messageStr,
                    ExecuteCommissionAndDividendsMessage.class
            );

            if (executeCommissionOrDividendsService.executeCommissionOrDividends(executeCommissionAndDividendsMessage)) {
                //处理成功 手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                log.info("[Rabbit消费执行分红和返佣队列消息] - 成功, 订单号: {}", executeCommissionAndDividendsMessage.getOrderNo());
            } else {
                log.error("[Rabbit消费执行分红和返佣队列消息] - 失败, 订单号: {}", executeCommissionAndDividendsMessage.getOrderNo());
                //处理失败 抛出异常 进行重试消费
                throw new RuntimeException();
            }
        } catch (Exception e) {
            log.error("[Rabbit消费执行分红和返佣队列消息] - 失败  e: {}", e.getMessage());
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
