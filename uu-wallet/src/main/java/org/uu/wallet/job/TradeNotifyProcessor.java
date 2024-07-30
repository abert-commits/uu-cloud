//package org.uu.wallet.job;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.constant.RabbitMqConstants;
//import org.uu.wallet.entity.MatchingOrder;
//import org.uu.wallet.entity.QueueInfo;
//import org.uu.wallet.service.IMatchingOrderService;
//import org.uu.wallet.service.IMerchantInfoService;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//import tech.powerjob.worker.core.processor.ProcessResult;
//import tech.powerjob.worker.core.processor.TaskContext;
//import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
//import tech.powerjob.worker.log.OmsLogger;
//
//import java.util.List;
//
//
//@Component("TradeNotifyProcessor")
//@Slf4j
//@RequiredArgsConstructor
//public class TradeNotifyProcessor implements BasicProcessor {
//    private final IMatchingOrderService matchingOrderService;
//    private final RabbitTemplate rabbitTemplate;
//    private final IMerchantInfoService merchantInfoService;
//
//    /*
//     * 定时任务-发送交易成功MQ消息
//     * */
//    @Override
//    public ProcessResult process(TaskContext context) {
//
//        //定时任务扫描10秒前 所有充值交易成功并且未发送MQ的订单
//        List<MatchingOrder> tradeSuccessAndUnsent = matchingOrderService.getTradeSuccessAndUnsent();
//
//        log.info("扫描10秒前 所有交易成功并且未发送MQ的订单: {}", JSON.toJSONString(tradeSuccessAndUnsent, SerializerFeature.WriteMapNullValue));
//
//        //发送充值交易成功MQ
//        for (MatchingOrder matchingOrder : tradeSuccessAndUnsent) {
//            //根据商户号获取验签key
//            matchingOrder.setKey(merchantInfoService.getMd5KeyByCode(matchingOrder.getCollectionMerchantCode()));
//            log.info("MQ发送充值交易成功的订单: {}", JSON.toJSONString(matchingOrder, SerializerFeature.WriteMapNullValue));
//
//            //MQ发送充值交易成功通知
//            QueueInfo queueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME, matchingOrder.getId(), matchingOrder.getCollectionMerchantOrder());
//            rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(queueInfo)));
//        }
//
//        //发送提现交易成功MQ
//        for (MatchingOrder matchingOrder : tradeSuccessAndUnsent) {
//            //根据商户号获取验签key
//            matchingOrder.setKey(merchantInfoService.getMd5KeyByCode(matchingOrder.getPaymentMerchantCode()));
//            log.info("MQ发送提现交易成功的订单: {}", JSON.toJSONString(matchingOrder, SerializerFeature.WriteMapNullValue));
//
//            //MQ发送充值交易成功通知
//            QueueInfo queueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, matchingOrder.getId(), matchingOrder.getPaymentMerchantOrder());
//            rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(queueInfo)));
//        }
//
//        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
//        OmsLogger omsLogger = context.getOmsLogger();
//        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());
//
//        return new ProcessResult(true, "result is xxx");
//    }
//}
