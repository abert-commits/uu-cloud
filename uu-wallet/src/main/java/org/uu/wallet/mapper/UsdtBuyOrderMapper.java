package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.uu.common.pay.dto.TodayUsdtOrderOverviewDTO;
import org.uu.wallet.entity.UsdtBuyOrder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
@Mapper
public interface UsdtBuyOrderMapper extends BaseMapper<UsdtBuyOrder> {

    /**
     * 根据订单号查询USDT买入订单 加上排他行锁
     *
     * @param platform_order
     * @return {@link UsdtBuyOrder}
     */
    @Select("SELECT * FROM usdt_buy_order WHERE platform_order = #{platform_order} FOR UPDATE")
    UsdtBuyOrder selectUsdtBuyOrderForUpdate(String platform_order);


    /**
     * 根据USDT买入订单id 更新USDT买入订单状态
     *
     * @param id
     * @param status
     * @return int
     */
    @Update("UPDATE usdt_buy_order SET status = #{status}, update_time = now() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);


    /**
     * 查询USDT买入订单 超过 minutes 分钟 状态为: 待支付
     *
     * @param status_be_paid
     * @param rechargeExpirationTime
     * @return {@link List}<{@link UsdtBuyOrder}>
     */
    @Select("SELECT id, status, platform_order " +
            "FROM usdt_buy_order " +
            "WHERE status = #{status_be_paid} " +
            "AND create_time <= DATE_SUB(NOW(), INTERVAL #{rechargeExpirationTime} MINUTE)")
    List<UsdtBuyOrder> findPendingUsdtBuyOrders(String status_be_paid, Integer rechargeExpirationTime);

    List<UsdtBuyOrder> selectSumInfo(@Param("dateStr") String dateStr);

    /**
     * 根据U地址查询 待支付的USDT买入订单 加上排他行锁 3: 待支付
     *
     * @param usdtAddr USDT地址
     * @return {@link UsdtBuyOrder}
     */
    @Select("SELECT * FROM usdt_buy_order WHERE usdt_addr = #{usdtAddr} AND status = 3 LIMIT 1 FOR UPDATE")
    UsdtBuyOrder selectPendingUsdtBuyOrderForAddressForUpdate(@Param("usdtAddr") String usdtAddr);


    /**
     * 根据平台订单号查询USDT充值订单
     * @param platformOrder
     * @return {@link UsdtBuyOrder}
     */
    @Select("SELECT * FROM usdt_buy_order WHERE platform_Order = #{platformOrder}")
    UsdtBuyOrder getusdtBuyOrderByPlatformOrder(String platformOrder);


    @Select("select count(1) as totalUsdtOrderCount,IFNULL(sum(arb_actual_num), 0) as totalITokenAmount,IFNULL(sum(usdt_actual_num), 0) as totalUsdtAmount from uu_wallet.usdt_buy_order where status = 7")
    TodayUsdtOrderOverviewDTO getUsdtBuyOrderTotal();

    @Select("select count(1) as todayUsdtOrderCount,IFNULL(sum(arb_actual_num), 0) as todayITokenAmount,IFNULL(sum(usdt_actual_num), 0) as todayUsdtAmount from uu_wallet.usdt_buy_order where status = 7 and create_time >= #{today}")
    TodayUsdtOrderOverviewDTO getUsdtBuyOrderToday(@Param("today") String today);
}
