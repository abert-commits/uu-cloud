//package org.uu.wallet.handler.check;
//
//import org.uu.common.core.annotation.HandlerAnnotation;
//import org.uu.wallet.entity.CollectionOrder;
//import org.uu.wallet.entity.MatchingOrder;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.handler.Handler;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@HandlerAnnotation(offset = 4)
//public class TheSamePersonMatchesHandler extends Handler {
//
//    private Handler handler;
//    @Override
//    public void setNextHandler(Handler handler){
//        this.handler = handler;
//    }
//    @Override
//    public List<MatchingOrder> handler(List<CollectionOrder> clist, PaymentOrder paymentOrder){
//        Map<String,List<CollectionOrder>> map  =  clist.stream().collect(Collectors.groupingBy(e->e.getMerchantCode()+e.getUserId()));
//        return this.handler.handler(clist,paymentOrder);
//
//    }
//
//}
