package org.uu.wallet.handler;


import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MatchingOrder;
import org.uu.wallet.entity.PaymentOrder;

import java.util.List;

public abstract class Handler {

    public abstract void setNextHandler(Handler handler);

    /*
     * 使用责任链模式匹配订单(用代付订单去匹配充值订单)
     * */
    public abstract List<MatchingOrder> handler(List<CollectionOrder> clist, PaymentOrder paymentOrder);
}
