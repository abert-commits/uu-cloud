//package org.uu.wallet.webSocket;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.service.ICollectionOrderService;
//import org.uu.wallet.service.IPaymentOrderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 推送推荐金额列表至前端(针对商户模式)
// *
// * @author Simon
// * @date 2023/11/08
// */
//@Component
//@Slf4j
//public class MerchantSendRecommendAmount {
//
//    @Autowired
//    IPaymentOrderService paymentOrderService;
//    @Autowired
//    ICollectionOrderService collectionOrderService;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private MerchantGetAmountWebSocketController merchantGetAmountWebSocketController;
//
//    public void send() {
//
//        //(代付池) 查询所有待匹配代付订单
//        List<PaymentOrder> payments = paymentOrderService.getPaymentOrderBySatus();
//        log.info("查询代付池: {}", JSON.toJSONString(payments, SerializerFeature.WriteMapNullValue));
//
//        HashMap<String, Integer> paymentAmountMap = new HashMap<>();
//
//        //从redis里面获取userId和充值金额
//        Map userIdAndCollectionAmount = redisTemplate.boundHashOps("userIdAndCollectionAmount").entries();
//
//        log.info("从redis里面获取userId和充值金额: {}", JSON.toJSONString(userIdAndCollectionAmount, SerializerFeature.WriteMapNullValue));
//
//        //遍历redis里面的数据(userId:充值金额)
//        for (Object userId : userIdAndCollectionAmount.keySet()) {
//            Object collectionAmount = userIdAndCollectionAmount.get(userId);
//            //遍历当前代付池里面的金额
//            for (PaymentOrder payment : payments) {
//                paymentAmountMap.put(payment.getMerchantOrder(), (int) Double.parseDouble(payment.getAmountStr()));
//            }
//
//            //查询最接近充值金额的前10笔代付订单
//            List<Map.Entry<String, Integer>> recommendAmounts = collectionOrderService.findClosestValues(paymentAmountMap, Integer.parseInt((String) collectionAmount), 10);
//
//            //根据webSocketID发送给前端推荐金额列表
//            boolean send = merchantGetAmountWebSocketController.AppointSending((String) userId, JSON.toJSONString(recommendAmounts, SerializerFeature.WriteMapNullValue));
//            if (send) {
//                log.info("webSocket推送推荐金额给前端成功: userId: {}, data: {}", userId, JSON.toJSONString(recommendAmounts, SerializerFeature.WriteMapNullValue));
//            } else {
//                log.info("webSocket推送推荐金额给前端失败: userId: {}, data: {}", userId, JSON.toJSONString(recommendAmounts, SerializerFeature.WriteMapNullValue));
//            }
//        }
//    }
//}
