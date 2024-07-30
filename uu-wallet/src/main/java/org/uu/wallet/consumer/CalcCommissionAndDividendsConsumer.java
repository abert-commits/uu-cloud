package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.message.CommissionAndDividendsMessage;
import org.uu.wallet.service.CalcCommissionAndDividendsService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings("all")
@RequiredArgsConstructor
public class CalcCommissionAndDividendsConsumer {
    public static final List<MemberAccountChangeEnum> BUY_AND_SELL_LIST = Arrays.asList(MemberAccountChangeEnum.RECHARGE, MemberAccountChangeEnum.WITHDRAW);

    private final CalcCommissionAndDividendsService calcCommissionAndDividendsService;

    @RabbitListener(queues = RabbitMqConstants.CALC_COMMISSION_AND_DIVIDENDS_QUEUE, concurrency = "5-10")
    public void calcCommissionAndDividends(Message message, Channel channel) {
        try {
            String messageStr = new String(message.getBody());
            log.info("[Rabbit消费计算分红和返佣队列消息] 消息内容: {}", messageStr);
            CommissionAndDividendsMessage commissionAndDividendsMessage = JSONObject.parseObject(messageStr, CommissionAndDividendsMessage.class);

            // 如果账变类型不是 买入/卖出 则不做任何处理
            if (!BUY_AND_SELL_LIST.contains(commissionAndDividendsMessage.getChangeType())) {
                //处理成功 手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                log.info("[Rabbit消费计算分红和返佣队列消息] - 失败, 消息内容: {}", messageStr);
                return;
            }

            if (
                    calcCommissionAndDividendsService.calcCommission(commissionAndDividendsMessage) &&
                            calcCommissionAndDividendsService.calcDividends(commissionAndDividendsMessage)
            ) {
                //处理成功 手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                log.info("[Rabbit消费计算分红和返佣队列消息] - 成功, 订单号: {}", commissionAndDividendsMessage.getOrderNo());
            } else {
                log.error("[Rabbit消费计算分红和返佣队列消息] - 失败, 订单号: {}", commissionAndDividendsMessage.getOrderNo());
                //处理失败 抛出异常 进行重试消费
                throw new RuntimeException();
            }
        } catch (Exception e) {
            log.error("[Rabbit消费计算分红和返佣队列消息] - 失败  e: {}", e.getMessage());
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
