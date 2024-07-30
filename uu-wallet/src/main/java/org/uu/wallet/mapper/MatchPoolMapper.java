package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.uu.wallet.entity.MatchPool;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Mapper
public interface MatchPoolMapper extends BaseMapper<MatchPool> {

    /**
     * 查询匹配池订单信息 加上排他行锁
     *
     * @param match_order
     * @return {@link MatchPool}
     */
    @Select("SELECT * FROM match_pool WHERE match_order = #{match_order} FOR UPDATE")
    MatchPool selectMatchPoolForUpdate(String match_order);


    /**
     * 根据匹配池订单id 更新匹配池订单状态
     *
     * @param id
     * @param orderStatus
     * @return int
     */
    @Update("UPDATE match_pool SET order_status = #{orderStatus} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("orderStatus") String orderStatus);


    /**
     * 根据匹配池订单id 更新匹配池订单状态 并将匹配超时 设置为 1
     *
     * @param id
     * @param orderStatus
     * @param matchTimeout
     * @return int
     */
    @Update("UPDATE match_pool SET order_status = #{orderStatus}, update_time = now(), match_timeout = #{matchTimeout} WHERE id = #{id}")
    int updateStatusAndMatchTimeout(@Param("id") Long id, @Param("orderStatus") String orderStatus, @Param("matchTimeout") String matchTimeout);


    /**
     * 查询匹配池订单 超过 minutes 分钟 状态为: 匹配中
     *
     * @param status_be_matched
     * @param memberSellMatchingDuration
     * @return {@link List}<{@link MatchPool}>
     */
    @Select("SELECT id, order_status, match_order " +
            "FROM match_pool " +
            "WHERE order_status = #{status_be_matched} " +
            "AND create_time <= DATE_SUB(NOW(), INTERVAL #{memberSellMatchingDuration} MINUTE)")
    List<MatchPool> findMatchingSellOrders(
            @Param("status_be_matched") String status_be_matched,
            @Param("memberSellMatchingDuration") int memberSellMatchingDuration);



    @Select("select IFNULL(count(1), 0) from uu_wallet.match_pool where order_status=#{orderStatus};")
    Long getOrderNumByOrderStatus(@Param("orderStatus") String orderStatus);


    /**
     * 根据金额查询匹配会员的卖出订单
     *
     * @param memberId
     * @param amount
     * @return
     */
    MatchPool selectMatchSellOrderByAmount(@Param("memberId") String memberId, @Param("amount") BigDecimal amount);
}
