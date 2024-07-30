//package org.uu.wallet.runable;
//
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.constant.RabbitMqConstants;
//import org.uu.wallet.entity.CollectionOrder;
//import org.uu.wallet.entity.MatchingOrder;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.entity.QueueInfo;
//import org.uu.wallet.handler.HandlerChain;
//import org.uu.wallet.service.ICollectionOrderService;
//import org.uu.wallet.service.IMatchingOrderService;
//import org.uu.wallet.service.IMerchantInfoService;
//import org.uu.wallet.service.IPaymentOrderService;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.CountDownLatch;
//
//
//@Slf4j
//public class PaymentOrderMatching implements Runnable {
//    private final CountDownLatch countDownLatch;
//    private final List<CollectionOrder> clist;
//    private final ICollectionOrderService collectionOrderService;
//    private final IPaymentOrderService paymentOrderService;
//    private final RabbitTemplate rabbitTemplate;
//    private PaymentOrder paymentOrder;
//    private IMatchingOrderService matchingOrderService;
//    private IMerchantInfoService merchantInfoService;
//
//
//    public PaymentOrderMatching(CountDownLatch countDownLatch, List<CollectionOrder> clist, PaymentOrder paymentOrder, IMatchingOrderService matchingOrderService, ICollectionOrderService collectionOrderService, IPaymentOrderService paymentOrderService, RabbitTemplate rabbitTemplate, IMerchantInfoService merchantInfoService) {
//        this.countDownLatch = countDownLatch;
//        this.clist = clist;
//        this.paymentOrder = paymentOrder;
//        this.matchingOrderService = matchingOrderService;
//        this.collectionOrderService = collectionOrderService;
//        this.paymentOrderService = paymentOrderService;
//        this.rabbitTemplate = rabbitTemplate;
//        this.merchantInfoService = merchantInfoService;
//    }
//
//    @Override
//    public void run() {
//        try {
//            //定时任务 扫描 匹配订单
//            doWork(clist, paymentOrder, matchingOrderService, collectionOrderService, paymentOrderService);
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            countDownLatch.countDown();
//        }
//    }
//
//    /**
//     * 定时任务 扫描 匹配订单
//     */
//    private void doWork(List<CollectionOrder> clist, PaymentOrder paymentOrder, IMatchingOrderService matchingOrderService, ICollectionOrderService collectionOrderService, IPaymentOrderService paymentOrderService) throws Exception {
//
//        log.info("定时任务匹配: 代付订单: {}, 支付池: {}", JSON.toJSONString(paymentOrder, SerializerFeature.WriteMapNullValue), JSON.toJSONString(clist, SerializerFeature.WriteMapNullValue));
//
//        Thread.sleep(Math.abs(new Random().nextInt() % 10000));
//        HandlerChain handlerChain = new HandlerChain("org.uu.wallet.handler.check");
//
//        //使用责任链模式匹配订单(用代付订单去匹配充值订单)
//        List<MatchingOrder> list = handlerChain.handler(clist, paymentOrder);
//
//        if (list != null && list.size() == 1) {
//
//            MatchingOrder matchingOrder = list.get(0);
//
//            //写入匹配订单表 已经在数据库订单号建立了唯一索引 若插入失败 则不做处理 等待下一次匹配
//            try {
//                System.out.println("matchingOrder: " + JSON.toJSONString(matchingOrder, SerializerFeature.WriteMapNullValue));
//
//                //1对1匹配成功
//
//                boolean save = matchingOrderService.save(matchingOrder);
//
//                System.out.println("save: " + save);
//
//                //更新代付订单匹配状态
//                paymentOrderService.updateMatchingStatusByOrderNo(paymentOrder.getMerchantOrder());
//
//                //更新代收订单匹配状态
//                collectionOrderService.updateMatchingStatusByOrderNo(matchingOrder.getCollectionMerchantOrder());
//
//                log.info("定时任务: 1对1匹配成功, 发送MQ消息: 代付订单号: {}, 支付订单号: {}, 订单信息: {}", paymentOrder.getMerchantOrder(), matchingOrder.getCollectionMerchantOrder(), JSON.toJSONString(list, SerializerFeature.WriteMapNullValue));
//
//                //根据商户号获取验签key
//                matchingOrder.setKey(merchantInfoService.getMd5KeyByCode(matchingOrder.getCollectionMerchantCode()));
//
//                QueueInfo queueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_MATCH_QUEUE_NAME, matchingOrder.getId(), matchingOrder.getPaymentMerchantOrder());
//
//                //发送MQ 回调商户订单匹配成功
//                rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_MATCH_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(queueInfo)));
//            } catch (Exception e) {
//                //匹配重复 该订单已存在
//                log.info("定时任务: 1对1匹配重复, 代付订单号: {}, 支付订单号: {}, 订单信息: {}", paymentOrder.getMerchantOrder(), matchingOrder.getCollectionMerchantOrder(), JSON.toJSONString(list, SerializerFeature.WriteMapNullValue));
//
//            }
//
//        } else if (list != null && list.size() > 1) {
//            try {
//                // 1对多匹配成功 (因为一笔代付会对应对比支付 所以取第一笔回调商户就可以了 match_orders存储了代付订单所对应的支付订单)
//                MatchingOrder matchingOrder = list.get(0);
//
//                //写入匹配订单表
//                matchingOrderService.saveBatch(list);
//
//                //更新代付订单匹配状态
//                paymentOrderService.updateMatchingStatusByOrderNo(paymentOrder.getMerchantOrder());
//
//                //获取所有匹配成功的支付订单号 批量更新匹配状态
//                ArrayList<String> collectionOrderList = new ArrayList<>();
//                for (MatchingOrder matchingOrder1 : list) {
//                    collectionOrderList.add(matchingOrder1.getCollectionMerchantOrder());
//                }
//                //更新代收订单匹配状态 (批量更新)
//                collectionOrderService.updateBatchMatchingStatusByOrderNos(collectionOrderList);
//
//                log.info("定时任务: 1对多匹配成功, 发送MQ: 代付订单号: {}, 订单信息: {}", paymentOrder.getMerchantOrder(), JSON.toJSONString(list, SerializerFeature.WriteMapNullValue));
//
//                //根据商户号获取验签key
//                matchingOrder.setKey(merchantInfoService.getMd5KeyByCode(matchingOrder.getCollectionMerchantCode()));
//
//                QueueInfo queueInfo = new QueueInfo(RabbitMqConstants.UU_WALLET_MATCH_QUEUE_NAME, matchingOrder.getId(), matchingOrder.getPaymentMerchantOrder());
//
//                //发送MQ 回调商户订单匹配成功
//                rabbitTemplate.convertAndSend(RabbitMqConstants.UU_WALLET_MATCH_QUEUE_NAME, matchingOrder, new CorrelationData(JSON.toJSONString(queueInfo)));
//            } catch (Exception e) {
//                log.info("定时任务: 1对多匹配重复, 代付订单号: {}, 订单信息: {}", paymentOrder.getMerchantOrder(), JSON.toJSONString(list, SerializerFeature.WriteMapNullValue));
//            }
//        } else {
//            // 未匹配到订单
//            log.info("定时任务: 匹配失败, 未找到合适的订单: {}", JSON.toJSONString(paymentOrder, SerializerFeature.WriteMapNullValue));
//        }
//    }
//}
