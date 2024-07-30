package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.uu.common.pay.dto.RelationOrderDTO;
import org.uu.common.pay.req.RelationshipOrderReq;
import org.uu.wallet.entity.MatchingOrder;
import org.uu.wallet.entity.MemberInfo;

import java.util.List;

/**
 * @author
 */
@Mapper
public interface MatchingOrderMapper extends BaseMapper<MatchingOrder> {

    void updateOrderStatusByOrderNo(@Param("orderNo") String orderNo, @Param("appealType") Integer appealType, @Param("status") Integer status);

    /**
     * 根据撮合列表订单号查询撮合列表订单 加上排他行锁
     *
     * @param platform_order
     * @return {@link MatchingOrder}
     */
    @Select("SELECT * FROM matching_order WHERE platform_order = #{platform_order} FOR UPDATE")
    MatchingOrder selectMatchingOrderForUpdate(String platform_order);


    /**
     * 根据撮合列表订单id 更新撮合列表订单状态
     *
     * @param id
     * @param status
     * @return int
     */
    @Update("UPDATE matching_order SET status = #{status}, update_time = now() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 查询撮合列表订单 状态为确认中
     *
     * @param status_confirmation
     * @param memberConfirmExpirationTime
     * @return {@link List}<{@link MatchingOrder}>
     */
    @Select("SELECT id, status, platform_order " +
            "FROM matching_order " +
            "WHERE status = #{status_confirmation} " +
            "AND create_time <= DATE_SUB(NOW(), INTERVAL #{memberConfirmExpirationTime} MINUTE)")
    List<MatchingOrder> findOrdersInConfirmation(
            @Param("status_confirmation") String status_confirmation,
            @Param("memberConfirmExpirationTime") int memberConfirmExpirationTime);

    MatchingOrder selectMatchingOrderByWithdrawOrder(@Param("withdrawOrder") String withdrawOrder);

    Long fethchAmountErrorNum();

    Long matchSuccessNum();


}
