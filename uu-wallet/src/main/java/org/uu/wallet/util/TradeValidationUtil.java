package org.uu.wallet.util;

import org.uu.wallet.Enum.OrderAmountValidationResult;

import java.math.BigDecimal;

public class TradeValidationUtil {

    /**
     * 判断订单金额是否在最小买入金额和最大卖出金额之间
     *
     * @param amount            订单金额
     * @param minPurchaseAmount 最小买入金额
     * @param maxSellAmount     最大卖出金额
     * @return 订单金额是否有效
     */
    public static OrderAmountValidationResult isOrderAmountValid(BigDecimal amount, BigDecimal minPurchaseAmount, BigDecimal maxSellAmount) {
        // 最小买入金额判断，最小金额不为null且订单金额小于最小买入金额时，返回false
        if (minPurchaseAmount != null && amount.compareTo(minPurchaseAmount) < 0) {
            return OrderAmountValidationResult.TOO_LOW; // 订单金额太少
        }

        // 最大卖出金额判断，最大金额不为null且订单金额大于最大卖出金额时，返回false
        if (maxSellAmount != null && amount.compareTo(maxSellAmount) > 0) {
            return OrderAmountValidationResult.TOO_HIGH; // 订单金额太多
        }

        // 订单金额在最小买入金额和最大卖出金额之间
        return OrderAmountValidationResult.VALID; // 订单金额有效
    }
}
