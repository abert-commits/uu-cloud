//package org.uu.wallet.job;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.wallet.entity.CollectionOrder;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.runable.PaymentOrderMatching;
//import org.uu.wallet.service.ICollectionOrderService;
//import org.uu.wallet.service.IMatchingOrderService;
//import org.uu.wallet.service.IMerchantInfoService;
//import org.uu.wallet.service.IPaymentOrderService;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//import tech.powerjob.worker.core.processor.ProcessResult;
//import tech.powerjob.worker.core.processor.TaskContext;
//import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
//import tech.powerjob.worker.log.OmsLogger;
//
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//
//@Component("matchingOrderProcessor")
//@Slf4j
//@RequiredArgsConstructor
//public class MatchingOrderProcessor implements BasicProcessor {
//    private final IMerchantInfoService merchantInfoService;
//    private final ICollectionOrderService collectionOrderService;
//    private final IPaymentOrderService paymentOrderService;
//    private final IMatchingOrderService matchingOrderService;
//    private final RabbitTemplate rabbitTemplate;
//
//
//    @Override
//    public ProcessResult process(TaskContext context) throws Exception {
//
//
//        // List<MerchantInfo> list = merchantInfoService.getAllMerchantByStatus();
//
//        //(代付池) 查询所有代付订单: 未成功 未匹配
//        List<PaymentOrder> payments = paymentOrderService.getPaymentOrderBySatus();
//        log.info("定时任务获取代付池: {}", JSON.toJSONString(payments, SerializerFeature.WriteMapNullValue));
//
//        //(支付池) 查询所有代收订单: 未成功 未匹配
//        List<CollectionOrder> ollections = collectionOrderService.getCollectionOrderBySatus();
//        log.info("定时任务获取支付池: {}", JSON.toJSONString(ollections, SerializerFeature.WriteMapNullValue));
//
//        //创建线程池
//        ExecutorService threadPool = Executors.newFixedThreadPool(ollections.size());
//
//        CountDownLatch downLatch = new CountDownLatch(payments.size());
//
//        //遍历代付池 使用代付订单去匹配支付订单
//        for (PaymentOrder paymentOrder : payments) {
//            PaymentOrderMatching paymentOrderMatching = new PaymentOrderMatching(downLatch, ollections, paymentOrder, matchingOrderService, collectionOrderService, paymentOrderService, rabbitTemplate, merchantInfoService);
//            threadPool.submit(paymentOrderMatching);
//        }
//
//        downLatch.await();
//
//
//        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
//        OmsLogger omsLogger = context.getOmsLogger();
//        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());
//
//        return new ProcessResult(true, "result is xxx");
//    }
//}
