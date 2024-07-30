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
//import org.uu.wallet.util.MatchesUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.StringJoiner;
//
//@Slf4j
//@HandlerAnnotation(offset = 2)
//public class AllMatchesHandler extends Handler {
//
//    private Handler handler;
//
//    @Override
//    public void setNextHandler(Handler handler) {
//        this.handler = handler;
//    }
//
//    /*
//     * 责任链第二步  1对多匹配: 组装多笔支付订单金额去匹配同一笔代付订单
//     * 使用责任链模式匹配订单(用代付订单去匹配充值订单)
//     * */
//    @Override
//    public List<MatchingOrder> handler(List<CollectionOrder> clist, PaymentOrder paymentOrder) {
//
//        log.info("责任链匹配第二步, 代付订单号:{}, 支付订单池: {}", paymentOrder.getMerchantOrder(), JSON.toJSONString(clist, SerializerFeature.WriteMapNullValue));
//
//        // 1对多匹配: 组装多笔支付订单金额去匹配同一笔代付订单
//        List<MatchingOrder> rlist = MatchesUtil.getMatchedOrder(clist, paymentOrder);
//
//        if (rlist != null && rlist.size() > 0) {
//            log.info("责任链匹配第二步(1对多)匹配成功, 匹配结束, rlist:{}", JSON.toJSONString(rlist, SerializerFeature.WriteMapNullValue));
//
//            //匹配到的订单号
//            for (MatchingOrder matchingOrder : rlist) {
//                ArrayList<String> matchOrdersList = new ArrayList<>();
//                for (MatchingOrder order : rlist) {
//                    matchOrdersList.add(order.getCollectionMerchantOrder());
//                }
//                StringJoiner matchOrdersStr = new StringJoiner(",");
//                for (String str : matchOrdersList) {
//                    matchOrdersStr.add(str);
//                }
//                matchingOrder.setMatchOrders(String.valueOf(matchOrdersStr));
//            }
//            return rlist;
//        }
//        return null;
//    }
//}
