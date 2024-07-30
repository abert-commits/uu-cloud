//package org.uu.wallet.handler.check;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.annotation.HandlerAnnotation;
//import org.uu.wallet.entity.CollectionOrder;
//import org.uu.wallet.entity.MatchingOrder;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.handler.Handler;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.StringJoiner;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@HandlerAnnotation(offset = 1)
//@Slf4j
//public class OneToOneMatchesHandler extends Handler {
//    private Handler handler;
//
//    @Override
//    public void setNextHandler(Handler handler) {
//        this.handler = handler;
//    }
//
//    /*
//     * 责任链第一步 1对1匹配
//     * 使用责任链模式匹配订单(用代付订单去匹配充值订单)
//     * */
//    @Override
//    public List<MatchingOrder> handler(List<CollectionOrder> clist, PaymentOrder paymentOrder) {
//
//        log.info("责任链匹配第一步, 代付订单号:{}, 支付订单池: {}", paymentOrder.getMerchantOrder(), JSON.toJSONString(clist, SerializerFeature.WriteMapNullValue));
//
//        //将支付池的数据转为Map key=金额  value=订单信息
//        Map<String, CollectionOrder> pmap = clist.stream().collect(Collectors.toMap(CollectionOrder::getAmountStr, Function.identity(), (key1, key2) -> key2));
//
//        log.info("责任链匹配第一步, 金额->订单信息: {}", JSON.toJSONString(pmap, SerializerFeature.WriteMapNullValue));
//
//        //匹配相同金额的支付订单
//        CollectionOrder collectionOrder = pmap.get(paymentOrder.getAmount().toString());
//
//        if (collectionOrder != null) {
//
//            log.info("责任链匹配第一步, 匹配到相同金额的支付订单: 代付订单号: {}, 支付订单号: {}", paymentOrder.getMerchantOrder(), collectionOrder.getMerchantOrder());
//
//            List<MatchingOrder> rlist = new ArrayList<MatchingOrder>();
//
//            MatchingOrder matchingOrder = new MatchingOrder();
//
//            //支付方式
//            matchingOrder.setPayType(collectionOrder.getPayType());
//
//            //充值商户订单号
//            matchingOrder.setCollectionMerchantOrder(collectionOrder.getMerchantOrder());
//
//            //充值平台订单号
//            matchingOrder.setCollectionPlatformOrder(collectionOrder.getPlatformOrder());
//
//            //提现商户订单号
//            matchingOrder.setPaymentMerchantOrder(paymentOrder.getMerchantOrder());
//
//            //提现平台订单号
//            matchingOrder.setPaymentPlatformOrder(paymentOrder.getPlatformOrder());
//
//            //充值金额
//            matchingOrder.setCollectionAmount(collectionOrder.getAmount());
//
//            //提现金额
//            matchingOrder.setPaymentAmount(paymentOrder.getAmount());
//
//            //国家
//            matchingOrder.setCountry(collectionOrder.getCountry());
//
//            //充值用户id
//            matchingOrder.setCollectionUserId(collectionOrder.getUserId());
//
//            //提现用户id
//            matchingOrder.setPaymentUserId(paymentOrder.getUserId());
//
//            //充值商户号
//            matchingOrder.setCollectionMerchantCode(collectionOrder.getMerchantCode());
//
//            //提现商户号
//            matchingOrder.setPaymentMerchantCode(paymentOrder.getMerchantCode());
//
//            //交易回调地址
//            matchingOrder.setTradeNotifyUrl(paymentOrder.getNotifyUrl());
//
//            //匹配回调地址
//            matchingOrder.setMatchNotifyUrl(paymentOrder.getMatchNotifyUrl());
//
//            //匹配到的订单号
//            ArrayList<String> matchOrdersList = new ArrayList<>();
//            matchOrdersList.add(collectionOrder.getMerchantOrder());
//
//            StringJoiner matchOrdersStr = new StringJoiner(",");
//            for (String str : matchOrdersList) {
//                matchOrdersStr.add(str);
//            }
//
//            matchingOrder.setMatchOrders(String.valueOf(matchOrdersStr));
//
//            rlist.add(matchingOrder);
//            log.info("责任链匹配第一步(1对1)匹配成功, 匹配结束, rlist:{}", JSON.toJSONString(rlist, SerializerFeature.WriteMapNullValue));
//
//            return rlist;
//        }
//        return this.handler.handler(clist, paymentOrder);
//    }
//}
