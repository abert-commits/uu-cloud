//package org.uu.wallet.consumer;
//
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.rabbitmq.client.Channel;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.constant.RabbitMqConstants;
//import org.uu.wallet.entity.MatchingOrder;
//import org.uu.wallet.service.impl.MatchingOrderServiceImpl;
//import org.uu.wallet.util.RequestUtil;
//import org.uu.wallet.util.SignAPI;
//import org.uu.wallet.util.SignUtil;
//import org.uu.wallet.util.SpringContextUtil;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
///**
// * MQ发送提现成功异步通知
// */
//@Component
//@Slf4j
//public class TradePaymentNotifyConsumer {
//    @RabbitListener(queues = RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, ackMode = "MANUAL")
//    public static void process(@Payload MatchingOrder matchingOrder, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Message message) {
//
//        log.info("MQ-提现交易回调开始...  matchingOrder: {}, channel: {},", JSON.toJSONString(matchingOrder, SerializerFeature.WriteMapNullValue), channel);
//
//        //发送提现成功回调
//        Map<String, Object> dataMap = new HashMap<>();
//
//        //提现商户号
//        dataMap.put("merchantCode", matchingOrder.getPaymentMerchantCode());
//
//        //提现商户订单号
//        dataMap.put("merchantOrder", matchingOrder.getPaymentMerchantOrder());
//
//        //提现平台订单号
//        dataMap.put("platformOrder", matchingOrder.getPaymentPlatformOrder());
//
//        //订单金额
//        dataMap.put("amount", matchingOrder.getOrderActualAmount());
//
//        //提现会员id
//        dataMap.put("memberId", matchingOrder.getPaymentMemberId());
//
//        //提现交易成功回调地址
//        dataMap.put("notifyUrl", matchingOrder.getPaymentTradeNotifyUrl());
//
//        //UPI_ID
//        dataMap.put("upiId", matchingOrder.getUpiId());
//
//        //UPI_Name
//        dataMap.put("upiName", matchingOrder.getUpiName());
//
//        //时间戳
//        dataMap.put("timestamp", System.currentTimeMillis());
//
//        //md5签名
//        String signinfo = SignUtil.sortData(dataMap, "&");
//
//        log.info("MQ-提现交易回调: 平台订单号: {}, dataMap: {}, MQ提现交易回调商户签名串: {}", matchingOrder.getPaymentPlatformOrder(), dataMap, signinfo);
//
//        String sign = SignAPI.sign(signinfo, matchingOrder.getKey());
//
//        log.info("MQ-提现交易回调: 平台订单号: {}, MQ提现交易交易回调验签Key: {}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getKey());
//
//        dataMap.put("sign", sign);
//
//        MatchingOrderServiceImpl matchingOrderService = SpringContextUtil.getBean(MatchingOrderServiceImpl.class);
//
//        String reqinfo = JSON.toJSONString(dataMap);
//
//        log.info("MQ-提现交易回调: 平台订单号: {}, MQ-提现交易回调: 商户请求地址: {}, MQ-提现交易回调: 商户请求数据: {}", matchingOrder.getPaymentPlatformOrder(), matchingOrder.getPaymentTradeNotifyUrl(), reqinfo);
//
//        try {
//            String resultCallBack = RequestUtil.HttpRestClientToJson(matchingOrder.getPaymentTradeNotifyUrl(), reqinfo);
//            log.info("MQ提现交易回调平台订单号: {}, MQ提现交易回调商户返回数据: {}", matchingOrder.getPaymentPlatformOrder(), resultCallBack);
//            if ("SUCCESS".equals(resultCallBack)) {
//
//                log.info("MQ提现交易回调平台订单号: {}, MQ提现交易回调成功: {}", matchingOrder.getPaymentPlatformOrder(), resultCallBack);
//
//                //更新提现交易回调订单状态为: 自动回调成功  更新交易回调时间 更新完成时长
//                boolean updateTradeCollectionSuccess = matchingOrderService.updateTradePaymentSuccess(matchingOrder.getPaymentMerchantOrder());
//
//                if (updateTradeCollectionSuccess) {
//                    //消费成功, 删除队列里面的消息
//                    channel.basicAck(deliveryTag, false);
//                }
//            } else {
//                log.info("MQ提现交易回调平台订单号: {}, MQ提现交易回调失败: {}, 商户未返回SUCCESS", matchingOrder.getPaymentPlatformOrder(), resultCallBack);
//                //更新交易回调状态为: 自动回调失败
//                matchingOrderService.updateTradePaymentFailed(matchingOrder.getPaymentMerchantOrder());
//            }
//        } catch (Exception e) {
//            log.info("MQ提现交易回调平台订单号: {}, MQ提现交易回调失败", matchingOrder.getPaymentPlatformOrder());
//            //更新交易回调状态为: 自动回调失败
//            matchingOrderService.updateTradePaymentFailed(matchingOrder.getPaymentMerchantOrder());
//            throw new RuntimeException(e);
//        }
//    }
//}
