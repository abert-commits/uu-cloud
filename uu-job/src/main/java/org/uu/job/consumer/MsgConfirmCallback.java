//package org.uu.job.consumer;
//
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.RequiredArgsConstructor;
//import org.uu.common.core.constant.RabbitMqConstants;
//import org.uu.manager.entity.CustomCorrelationData;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
//import org.springframework.stereotype.Component;
//
///**
// * 处理消息确认
// *
// * @author Simon
// * @date 2023/11/21
// */
//@Component
//@RequiredArgsConstructor
//public class MsgConfirmCallback implements ConfirmCallback {
//
//    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);
//
//    @Override
//    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//
//        //校验 CorrelationData 格式是否正确
//        if (correlationData instanceof CustomCorrelationData) {
//
//            CustomCorrelationData customData = (CustomCorrelationData) correlationData;
//
//            if (customData != null) {
//                if (ack) {
//                    switch (customData.getQueueName()) {
//                        case RabbitMqConstants.WALLET_MEMBER_DAILY_TRADE_CLEUU_QUEUE:
//                            //钱包项目-清空每日交易数据队列
//                            logger.info("清空每日交易数据队列 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
//                            //更新充值交易MQ发送状态
////                            matchingOrderService.updateCollectionTradeSend(customData.getOrderNo());
//                            break;
//                        case RabbitMqConstants.WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_QUEUE:
//                            //钱包项目-清空每日交易数据队列
//                            logger.info("完成实名认证任务 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
//                            //更新充值交易MQ发送状态
////                            matchingOrderService.updateCollectionTradeSend(customData.getOrderNo());
//                            break;
//                        default:
//                            logger.warn("未知的队列名称: " + customData.getQueueName());
//                            break;
//                    }
//                } else {
//                    //这里做MQ重发消息机制 (nacos配置了发送端的重试机制 所以会自动触发消息重发机制)
//                    logger.info("清空每日交易数据队列 消息发送失败: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
//                }
//            } else {
//                logger.error("MQ消息确认 CustomCorrelationData 为null...");
//            }
//        } else {
//            logger.error("MQ消息确认 correlationData 格式不正确...");
//        }
//    }
//}