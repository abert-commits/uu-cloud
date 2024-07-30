package org.uu.wallet.service;

/**
 * UPI收款信息单日数据统计
 *
 * @author Simon
 * @date 2024/03/05
 */
public interface UpiTransactionService {


    /**
     * 增加单日交易笔数并且标记为已处理
     *
     * @param memberId
     * @param orderId
     */
    void incrementDailyTransactionCountAndMarkAsProcessed(String memberId, String orderId);

    /**
     * 减少当日收款次数
     *
     * @param memberId
     * @param orderId
     */
    void decrementDailyTransactionCountIfApplicable(String memberId, String orderId);

    /**
     * 获取单日交易笔数
     *
     * @param memberId
     * @return {@link Long}
     */
    Long getDailyTransactionCount(String memberId);


    /**
     * 生成交易笔数的键
     *
     * @param upiId
     * @return {@link String}
     */
    String generateTransactionCountKey(String upiId);


    /**
     * 设置键的过期时间为当天午夜
     *
     * @param key
     */
    void setExpirationAtMidnight(String key);


    /**
     * 计算当前时间至午夜的秒数
     *
     * @return long
     */
    long calculateSecondsUntilMidnight();
}
