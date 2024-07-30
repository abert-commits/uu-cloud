package org.uu.wallet.service;

import java.math.BigDecimal;

/**
 * UPI收款信息单日数据统计
 *
 * @author Simon
 * @date 2024/03/05
 */
public interface IMemberTransactionService {

    /**
     * 更新会员每日交易信息
     *
     * @param buyMemberId
     * @param buyAmount
     * @param sellMemberId
     * @param sellAmount
     */
//    void updateMemberDailyTransactionInfo(String buyMemberId, BigDecimal buyAmount, String sellMemberId, BigDecimal sellAmount);


    /**
     * 获取今日买入金额
     *
     * @param memberId
     * @return {@link Double}
     */
    Double getBuyAmount(String memberId);

    /**
     * 获取今日买入次数
     *
     * @param memberId
     * @return {@link Double}
     */
    Double getBuyCount(String memberId);

    /**
     * 获取今日卖出金额
     *
     * @param memberId
     * @return {@link Double}
     */
    Double getSellAmount(String memberId);

    /**
     * 获取今日卖出次数
     *
     * @param memberId
     * @return {@link Double}
     */
    Double getSellCount(String memberId);

}
