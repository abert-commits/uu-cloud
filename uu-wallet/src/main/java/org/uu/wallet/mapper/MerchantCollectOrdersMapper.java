package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.common.pay.dto.LastOrderWarnDTO;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MerchantCollectOrders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户代收订单表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-01-05
 */
@Mapper
public interface MerchantCollectOrdersMapper extends BaseMapper<MerchantCollectOrders> {


    /**
     * 根据订单号查询代收订单 加上排他行锁
     *
     * @param platform_order
     * @return {@link CollectionOrder}
     */
    @Select("SELECT * FROM merchant_collect_orders WHERE platform_order = #{platform_order} FOR UPDATE")
    MerchantCollectOrders selectMerchantCollectOrdersForUpdate(String platform_order);

    /**
     * 根据商户订单号查询代收订单 加上排他行锁
     *
     * @param merchantOrder
     * @return {@link CollectionOrder}
     */
    @Select("SELECT * FROM merchant_collect_orders WHERE merchant_order = #{merchantOrder} FOR UPDATE")
    MerchantCollectOrders selectMerchantCollectOrdersByMerchantOrderForUpdate(String merchantOrder);

    Long selectRechargeNum();

    List<MemberInfo> selectMerchantRechargeNum();

    List<MerchantCollectOrders> selectCountGroupByCode(@Param("startTime")String startTime, @Param("endTime")String endTime);

    List<MemberInfo> selectCostByDate(@Param("dateStr")String dateStr);

    List<LastOrderWarnDTO> getCollectLastOrderCreditTime();

    /**
     * 获取会员支付中的代收订单 加上排他行锁
     * 查询条件: 支付中 匹配钱包订单
     * 如果有多条 只取最新的一条记录
     *
     * @param memberId
     * @return {@link MerchantCollectOrders }
     */
    @Select("SELECT * FROM merchant_collect_orders " +
            "WHERE member_id = #{memberId} " +
            "AND order_status = 1 " +
            "AND matched = 1 " +
            "AND buy_order_no IS NULL " +
            "ORDER BY create_time DESC " +
            "LIMIT 1 " +
            "FOR UPDATE")
    MerchantCollectOrders selectLatestPendingRechargeOrderForUpdate(@Param("memberId") String memberId);

}
