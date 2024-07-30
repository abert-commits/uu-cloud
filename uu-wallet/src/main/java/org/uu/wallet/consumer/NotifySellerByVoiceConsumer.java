package org.uu.wallet.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.service.IPaymentOrderService;
import org.uu.wallet.thirdParty.TelephoneClient;
import org.uu.wallet.thirdParty.TelephoneStatus;
import org.uu.wallet.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 语音通知会员 (监听死信队列)
 *
 * @author Simon
 * @date 2023/12/21
 */
@Service
@RequiredArgsConstructor
public class NotifySellerByVoiceConsumer {

    //语音通知会员
    private final TelephoneClient telephoneClient;

    private final IMemberInfoService memberInfoService;

    private final IPaymentOrderService paymentOrderService;

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @RabbitListener(queues = RabbitMqConstants.WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_QUEUE, concurrency = "5-10")
    public void onTimeoutTask(Message message, Channel channel) {
        String messageBody = new String(message.getBody());

        TaskInfo taskInfo = JSON.parseObject(messageBody, TaskInfo.class);

        try {
            //语音通知会员
            logger.info("Rabbit 消费延时队列消息: 语音通知会员: {}", taskInfo.getOrderNo());

            //获取卖出订单信息
            PaymentOrder paymentOrder = paymentOrderService.getPaymentOrderByOrderNo(taskInfo.getOrderNo());
            if (paymentOrder != null) {
                MemberInfo memberInfo = memberInfoService.getById(paymentOrder.getMemberId());

                if (memberInfo != null) {
                    ArrayList<String> objects = new ArrayList<>();

                    String telephone = StringUtil.startsWith91(memberInfo.getMobileNumber()) ? memberInfo.getMobileNumber() : "91" + memberInfo.getMobileNumber();

                    objects.add(telephone);

                    List<TelephoneStatus> telephoneStatuses = telephoneClient.sendVoice(objects);

                    for (TelephoneStatus telephoneStatus : telephoneStatuses) {
                        if (telephoneStatus != null && telephoneStatus.getStatus() == true) {
                            //处理成功 手动确认消息
                            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                            logger.info("语音通知会员, 消息消费成功: {}", taskInfo.getOrderNo());
                        } else {
                            logger.error("语音通知会员, 消息消费失败: {}", taskInfo.getOrderNo());
                            //处理失败 抛出异常 进行重试消费
                            throw new RuntimeException();
                        }
                    }
                } else {
                    logger.error("语音通知会员, 获取会员信息失败, 订单号: {}", taskInfo.getOrderNo());
                }
            } else {
                logger.error("语音通知会员, 获取订单信息失败, 订单号: {}", taskInfo.getOrderNo());
            }
        } catch (Exception e) {
            logger.error("Rabbit MQ处理延时订单超时任务失败: e: {}", e);
            // 抛出异常, 触发重试机制
            throw new RuntimeException();
        }
    }
}
