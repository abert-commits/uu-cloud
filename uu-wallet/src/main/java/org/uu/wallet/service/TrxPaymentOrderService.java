package org.uu.wallet.service;

import org.springframework.stereotype.Service;

/**
 * 处理USDT代付订单
 *
 * @author simon
 * @date 2024/07/04
 */
@Service
public interface TrxPaymentOrderService {

    /**
     * 处理USDT代付订单
     *
     * @param platformOrder
     * @return {@link Boolean }
     */
    Boolean trxPaymentOrder(String platformOrder);
}
