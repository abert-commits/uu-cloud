//package org.uu.wallet.job;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.service.ICollectionOrderService;
//import org.uu.wallet.service.IPaymentOrderService;
//import org.uu.wallet.webSocket.WebSocketService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import tech.powerjob.worker.core.processor.ProcessResult;
//import tech.powerjob.worker.core.processor.TaskContext;
//import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
//import tech.powerjob.worker.log.OmsLogger;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//@Component("RecommendAmountProcessor")
//@Slf4j
//@RequiredArgsConstructor
//public class RecommendAmountProcessor implements BasicProcessor {
//    private final IPaymentOrderService paymentOrderService;
//    private final ICollectionOrderService collectionOrderService;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private WebSocketService webSocketService;
//
//    /*
//     * 定时任务查询所有待匹配的代付订单
//     * */
//    @Override
//    public ProcessResult process(TaskContext context) {
//
//
//        //(代付池) 查询所有代付订单: 条件: 待匹配状态
//        List<PaymentOrder> payments = paymentOrderService.getPaymentOrderBySatus();
//        log.info("定时任务->查询代付池: {}", JSON.toJSONString(payments, SerializerFeature.WriteMapNullValue));
//
//        HashMap<String, Integer> paymentAmountMap = new HashMap<>();
//
//        //从redis里面获取userId和充值金额
//        Map userIdAndCollectionAmount = redisTemplate.boundHashOps("userIdAndCollectionAmount").entries();
//
//        log.info("定时任务->从redis里面获取userId和充值金额: {}", JSON.toJSONString(userIdAndCollectionAmount, SerializerFeature.WriteMapNullValue));
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
//            boolean send = webSocketService.AppointSending((String) userId, JSON.toJSONString(recommendAmounts, SerializerFeature.WriteMapNullValue));
//            if (send) {
//                log.info("webSocket推送推荐金额给前端成功: userId: {}, data: {}", userId, JSON.toJSONString(recommendAmounts, SerializerFeature.WriteMapNullValue));
//            } else {
//                log.info("webSocket推送推荐金额给前端失败: userId: {}, data: {}", userId, JSON.toJSONString(recommendAmounts, SerializerFeature.WriteMapNullValue));
//            }
//
//
//        }
//
//        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
//        OmsLogger omsLogger = context.getOmsLogger();
//        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());
//
//        return new ProcessResult(true, "result is xxx");
//    }
//}
