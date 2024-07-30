package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.uu.common.pay.dto.MemberOrderOverviewDTO;
import org.uu.common.pay.dto.MerchantOrderOverviewDTO;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.vo.OrderInfoVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Mapper
public interface CollectionOrderMapper extends BaseMapper<CollectionOrder> {

    Long queryPayTotalNum();

    Long queryPayFinishNum();

    Long queryPayNotCallNum();

    Long queryPayCallFailedNum();


    Long queryPayTotalNumByName(@Param("name") String name);

    Long queryPayFinishNumByName(@Param("name") String name);

    Long queryPayNotCallNumByName(@Param("name") String name);

    Long queryPayCallFailedNumByName(@Param("name") String name);

    Boolean updateOrderStatusById(@Param("id") Long id);


    CollectionOrder getOrderByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据订单号查询收款订单 加上排他行锁
     *
     * @param platform_order
     * @return {@link CollectionOrder}
     */
    @Select("SELECT * FROM collection_order WHERE platform_order = #{platform_order} FOR UPDATE")
    CollectionOrder selectCollectionOrderForUpdate(String platform_order);


    /**
     * 根据买入订单id 更新买入订单状态
     * @param id
     * @param orderStatus
     * @return int
     */
    @Update("UPDATE collection_order SET order_status = #{orderStatus}, update_time = now() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);

    int updateCollectionByOrderNo(@Param("orderNo") String orderNo, @Param("orderStatus") String orderStatus,
                                  @Param("updateBy")String updateBy, @Param("actualAmount") BigDecimal actualAmount);


    // 查询买入订单 超过 minutes 分钟 状态为: 待支付
    @Select("SELECT id, order_status, platform_order " +
            "FROM collection_order " +
            "WHERE order_status = #{status_be_paid} " +
            "AND create_time <= DATE_SUB(NOW(), INTERVAL #{rechargeExpirationTime} MINUTE)")
    List<CollectionOrder> findPendingBuyOrders(
            @Param("status_be_paid") String status_be_paid,
            @Param("rechargeExpirationTime") Integer rechargeExpirationTime);

    Long queryPayCancelNum();

    Long queryPayCancelOrderNum();

    Long merchantPaymentOrderNum();

    OrderInfoVo fetchTodayBuyInfoFuture(@Param("dateStr")String dateStr);

    Long todayBuyInfoFuture(@Param("dateStr")String dateStr);

    OrderInfoVo fetchBuyTotalInfoFuture();

    BigDecimal calcTodayPayAmount(@Param("name") String name, @Param("dateStr") String dateStr);

    BigDecimal calcTodayPayCommission(@Param("name") String name, @Param("dateStr") String dateStr);

    Long calcTodayPayFinishNum(@Param("name") String name, @Param("dateStr") String dateStr);

    BigDecimal todayMerchantPayAmount(@Param("dateStr") String dateStr);

    Long todayMerchantPayTransNum(@Param("dateStr") String dateStr);

    BigDecimal merchantPayTotalAmount();

    Long merchantPayTransTotalNum();

    Long todayMerchantPayTransTotalNum(@Param("dateStr") String dateStr);

    BigDecimal todayMerchantPayCommission(@Param("dateStr") String dateStr);

    BigDecimal merchantPayTotalCommission();

    Long queryPayAppealTotalNum();

    /**
     * 根据支付订单状态获取数量
     * @param orderStatus orderStatus
     * @return long
     */
    @Select("select IFNULL(count(1),0) from collection_order where order_status = #{orderStatus}")
    Long getOrderNumByOrderStatus(@Param("orderStatus") String orderStatus);


    /**
     * 根据订单号查询收款订单 加上排他行锁
     *
     * @param merchantOrder
     * @return {@link CollectionOrder}
     */
    @Select("SELECT * FROM collection_order WHERE merchant_order = #{merchantOrder} FOR UPDATE")
    CollectionOrder selectCollectionOrderByMerchantOrderForUpdate(String merchantOrder);
}
