package org.uu.wallet.service;


/**
 * 处理超时订单
 *
 * @author Simon
 * @date 2023/12/01
 */
public interface HandleOrderTimeoutService {

    /**
     * 买入订单支付超时处理
     *
     * @param platformOrder
     * @return {@link Boolean}
     */
    Boolean handlePaymentTimeout(String platformOrder);


    /**
     * USDT支付超时处理
     *
     * @param platformOrder
     * @return {@link Boolean}
     */
    Boolean handleUsdtPaymentTimeout(String platformOrder);

}
