package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.uu.common.pay.dto.MemberOrderOverviewDTO;
import org.uu.common.pay.dto.MerchantOrderOverviewDTO;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.vo.OrderInfoVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    Long queryWithdrawTotalNum();

    Long queryWithdrawFinishNum();

    Long queryWithdrawNotCallNum();

    Long queryWithdrawCallFailedNum();


    Long queryWithdrawTotalNumByName(@Param("name") String name);

    Long queryWithdrawFinishNumByName(@Param("name") String name);

    Long queryWithdrawNotCallNumByName(@Param("name") String name);

    Long queryWithdrawCallFailedNumByName(@Param("name") String name);

    boolean updateOrderStatusById(@Param("id") Long id);

    /**
     * 根据订单号 查询卖出订单 加上排他行锁
     *
     * @param platform_order
     * @return {@link PaymentOrder}
     */
    @Select("SELECT * FROM payment_order WHERE platform_order = #{platform_order} FOR UPDATE")
    PaymentOrder selectPaymentForUpdate(String platform_order);


    /**
     * 根据卖出订单id 更新卖出订单状态
     *
     * @param id
     * @param orderStatus
     * @return int
     */
    @Update("UPDATE payment_order SET order_status = #{orderStatus}, update_time = now() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);


    /**
     * 根据卖出订单id 更新卖出订单状态 并将匹配超时设置为1
     *
     * @param id
     * @param orderStatus
     * @return int
     */
    @Update("UPDATE payment_order SET order_status = #{orderStatus}, update_time = now(), match_timeout = #{matchTimeout} WHERE id = #{id}")
    int updateStatusAndMatchTimeout(@Param("id") Long id, @Param("orderStatus") String orderStatus, @Param("matchTimeout") String matchTimeout);


    /**
     * 根据卖出订单id 更新卖出订单状态和匹配时间戳
     *
     * @param id
     * @param orderStatus
     * @param lastUpdateTimestamp
     * @return int
     */
    @Update("UPDATE payment_order SET order_status = #{orderStatus}, update_time = now(), last_update_timestamp = #{lastUpdateTimestamp} WHERE id = #{id}")
    int updateToReassignMatch(@Param("id") Long id, @Param("orderStatus") String orderStatus, @Param("lastUpdateTimestamp") Long lastUpdateTimestamp);


    /**
     * 查询卖出订单 超过 minutes 分钟 状态为: 匹配中
     *
     * @param status_be_matched
     * @param memberSellMatchingDuration
     * @return {@link List}<{@link PaymentOrder}>
     */
    @Select("SELECT id, order_status, platform_order " +
            "FROM payment_order " +
            "WHERE order_status = #{status_be_matched} " +
            "AND create_time <= DATE_SUB(NOW(), INTERVAL #{memberSellMatchingDuration} MINUTE)")
    List<PaymentOrder> findMatchingSellOrders(
            @Param("status_be_matched") String status_be_matched,
            @Param("memberSellMatchingDuration") int memberSellMatchingDuration);

    Long withdrawOverTimeNumFuture();

    Long withdrawCancelMatchNum();

    Long merchantCollectionOrderNum();

    void updateOrderStatusByOrderNo(@Param("orderNo") String orderNo, @Param("orderStatus") String orderStatus, @Param("updateBy")String updateBy);

    void updatePaymentForFinish(@Param("orderNo") String orderNo, @Param("orderStatus") String orderStatus, @Param("updateBy")String updateBy);

    void updateOrderByOrderNo(@Param("orderNo") String orderNo, @Param("orderStatus") String orderStatus,
                              @Param("updateBy")String updateBy, @Param("actualAmount") BigDecimal actualAmount);

    OrderInfoVo fetchTodaySellInfoFuture(@Param("dateStr")String dateStr);

    Long todaySellInfoFuture(@Param("dateStr")String dateStr);

    OrderInfoVo fetchSellTotalInfoFuture();

    OrderInfoVo fetchTodayUsdtInfoFuture(@Param("dateStr")String dateStr);

    OrderInfoVo fetchUsdtTotalInfoFuture();

    BigDecimal calcTodayWithdrawAmount(@Param("name") String name, @Param("dateStr") String dateStr);

    BigDecimal calcTodayWithdrawCommission(@Param("name") String name, @Param("dateStr") String dateStr);

    Long calcTodayWithdrawFinishNum(@Param("name") String name, @Param("dateStr") String dateStr);

    BigDecimal todayMerchantWithdrawAmount(@Param("dateStr") String dateStr);

    Long todayMerchantWithdrawTransNum(@Param("dateStr") String dateStr);

    BigDecimal merchantWithdrawTotalAmount();

    Long merchantWithdrawTransTotalNum();

    BigDecimal todayMerchantWithdrawCommission(@Param("dateStr") String dateStr);

    BigDecimal merchantWithdrawTotalCommission();

    Long withdrawAppealTotalNum();

    /**
     * 根据订单状态获取该状态下订单数量
     * @param orderStatus 状态
     * @return long
     */
    @Select("select IFNULL(count(1),0) from payment_order where order_status = #{orderStatus}")
    Long getOrderNumByOrderStatus(@Param("orderStatus") String orderStatus);
    MemberOrderOverviewDTO getMemberUsdtInfo(String startTime, String endTime);

    /**
     * 根据platform_order查询卖出订单信息
     *
     * @param platform_order
     * @return {@link PaymentOrder}
     */
    @Select("SELECT * FROM payment_order WHERE platform_order = #{orderNo}")
    PaymentOrder getPaymentOrderByPlatformOrder(@Param("orderNo") String platform_order);

    /**
     * 根据订单号 查询卖出订单 加上排他行锁
     *
     * @param merchantOrder
     * @return {@link PaymentOrder}
     */
    @Select("SELECT * FROM payment_order WHERE merchant_order = #{merchantOrder} FOR UPDATE")
    PaymentOrder selectPaymentByMerchantOrderForUpdate(String merchantOrder);
}
