package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.AppealOrder;

import java.util.List;

/**
 * @author
 */
@Mapper
public interface AppealOrderMapper extends BaseMapper<AppealOrder> {

    AppealOrder queryAppealOrderByOrderNo(@Param("orderNo") String orderNo, @Param("appealType")Integer appealType);

    AppealOrder queryAppealOrderByNo(@Param("orderNo") String orderNo);


    /**
     * 根据卖出订单号 查询申诉订单 加上排他行锁
     *
     * @param withdrawOrderNo
     * @return {@link AppealOrder}
     */
    @Select("SELECT * FROM appeal_order WHERE withdraw_order_no = #{withdrawOrderNo} FOR UPDATE")
    AppealOrder selectAppealOrderBywithdrawOrderNoForUpdate(String withdrawOrderNo);


    /**
     * 根据买入订单号 查询申诉订单 加上排他行锁
     *
     * @param rechargeOrderNo
     * @return {@link AppealOrder}
     */
    @Select("SELECT * FROM appeal_order WHERE recharge_order_no = #{rechargeOrderNo} FOR UPDATE")
    AppealOrder selectAppealOrderByRechargeOrderNoForUpdate(String rechargeOrderNo);

    List<AppealOrder> selectAppealNum(@Param("dateStr") String dateStr, @Param("appealType")Integer appealType);

    Long selectAppealTotalNum(@Param("dateStr") String dateStr, @Param("appealType")Integer appealType);

    Long selectWrongAmountNum(@Param("dateStr") String dateStr, @Param("appealType")Integer appealType, @Param("type") Integer type);
}
