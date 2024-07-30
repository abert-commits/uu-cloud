package org.uu.wallet.service;

import org.uu.wallet.entity.NotifyOrderStatusChangeMessage;

public interface OrderChangeEventService {

    /**
     * 处理订单状所有状态变化的事件("卖出"除外)
     *
     * @param orderStatusChangeMessage
     */
    void process(NotifyOrderStatusChangeMessage orderStatusChangeMessage);

    /**
     * 卖出订单处理
     *
     * @param orderStatusChangeMessage
     */
    void processSellOrder(NotifyOrderStatusChangeMessage orderStatusChangeMessage);

    /**
     * 处理取消卖出订单(无撮合订单)
     *
     * @param orderStatusChangeMsg
     */
    void processCancelSellOrder(NotifyOrderStatusChangeMessage orderStatusChangeMsg);
}
