//package org.uu.wallet.util;
//
//import org.uu.wallet.entity.CollectionOrder;
//import org.uu.wallet.entity.MatchingOrder;
//import org.uu.wallet.entity.PaymentOrder;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class MatchesUtil {
//
//
//    public static List<MatchingOrder> sum_M(List<CollectionOrder> list, PaymentOrder paymentOrder) {
//        int max = 1 << list.size();
//        for (int i = 1; i < max - 1; i++) {
//            BigDecimal s = new BigDecimal(0);
//            int k = list.size() - 1;
//            int t = i;
//            while (t > 0) {
//                if ((t & 1) > 0) {
//                    s = s.add(list.get(k).getAmount());
//                }
//                k--;
//                t = t >> 1;
//            }
//            if (s.compareTo(paymentOrder.getAmount()) == 0) {
//                return showResult(list, i, paymentOrder);
//            }
//
//        }
//        return null;
//    }
//
//    private static List<MatchingOrder> showResult(List<CollectionOrder> list, int i, PaymentOrder paymentOrder) {
//        List<MatchingOrder> rlist = new ArrayList<MatchingOrder>();
//        int k = list.size() - 1;
//        int t = i;
//        while (t > 0) {
//            if ((t & 1) > 0) {
//                MatchingOrder matchingOrder = new MatchingOrder();
//
//                //充值金额
//                matchingOrder.setCollectionAmount(list.get(k).getAmount());
//
//                //充值商户订单号
//                matchingOrder.setCollectionMerchantOrder(list.get(k).getMerchantOrder());
//
//                //充值平台订单号
//                matchingOrder.setCollectionPlatformOrder(list.get(k).getPlatformOrder());
//
//                //充值用户id
//                matchingOrder.setCollectionUserId(list.get(k).getUserId());
//
//                //充值商户号
//                matchingOrder.setCollectionMerchantCode(list.get(k).getMerchantCode());
//
//                //币种
//                matchingOrder.setCurrency(list.get(k).getCurrency());
//
//                //支付方式
//                matchingOrder.setPayType(list.get(k).getPayType());
//
//                //国家
//                matchingOrder.setCountry(list.get(k).getCountry());
//
//                //提现金额
//                matchingOrder.setPaymentAmount(paymentOrder.getAmount());
//
//                //提现商户号
//                matchingOrder.setPaymentMerchantCode(paymentOrder.getMerchantCode());
//
//                //提现用户id
//                matchingOrder.setPaymentUserId(paymentOrder.getUserId());
//
//                //提现商户订单号
//                matchingOrder.setPaymentMerchantOrder(paymentOrder.getMerchantOrder());
//
//                //提现平台订单号
//                matchingOrder.setPaymentPlatformOrder(paymentOrder.getPlatformOrder());
//
//                //交易回调地址
//                matchingOrder.setTradeNotifyUrl(paymentOrder.getNotifyUrl());
//
//                //匹配回调地址
//                matchingOrder.setMatchNotifyUrl(paymentOrder.getMatchNotifyUrl());
//
//                rlist.add(matchingOrder);
//                // System.out.print("  "+list.get(k).getAmount());
//            }
//            k--;
//            t = t >> 1;
//        }
//        //System.out.print("  = "+m);
//        return rlist;
//    }
//
////    public static void main(String[] args) {
////        List<MatchingOrder> rlist = getMatchedOrder(new ArrayList<CollectionOrder>(),new PaymentOrder());
////        if(rlist.size()>0){
////            System.out.println("匹配成功");
////        }
////    }
//
//    public static List<MatchingOrder> getMatchedOrder(List<CollectionOrder> list, PaymentOrder paymentOrder) {
////         MatchesUtil subArrSumUtil = new MatchesUtil();
////         paymentOrder.setAmount(new BigDecimal(100));
////         CollectionOrder  collectionOrder1 = new CollectionOrder();
////         collectionOrder1.setAmount(new BigDecimal(20));
////         CollectionOrder  collectionOrder2 = new CollectionOrder();
////         collectionOrder2.setAmount(new BigDecimal(30));
////         CollectionOrder  collectionOrder3 = new CollectionOrder();
////         collectionOrder3.setAmount(new BigDecimal(40));
////         CollectionOrder  collectionOrder4 = new CollectionOrder();
////         collectionOrder4.setAmount(new BigDecimal(10));
////
////         CollectionOrder  collectionOrder5 = new CollectionOrder();
////         collectionOrder5.setAmount(new BigDecimal(0));
////         list.add(collectionOrder1);
////         list.add(collectionOrder2);
////         list.add(collectionOrder3);
////         list.add(collectionOrder4);
////         list.add(collectionOrder5);
//
//        CollectionOrder collectionOrder5 = new CollectionOrder();
//        collectionOrder5.setAmount(new BigDecimal(0));
//        list.add(collectionOrder5);
//
//
//        return sum_M(list, paymentOrder);
//    }
//}
