package org.uu.wallet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.DelegationOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.uu.wallet.entity.MemberInfo;

/**
 * <p>
 * 委托订单表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-07-07
 */
@Mapper
public interface DelegationOrderMapper extends BaseMapper<DelegationOrder> {

    /**
     * 查询委托订单 加上排他行锁
     *
     * @param orderId
     * @return {@link MemberInfo}
     */
    @Select("SELECT * FROM delegation_order WHERE order_id = #{orderId} FOR UPDATE")
    DelegationOrder selectByOrderIdForUpdate(String orderId);

    /**
     * 查询委托订单 加上排他行锁
     *
     * @param memberId
     * @return {@link MemberInfo}
     */
    @Select("SELECT * FROM delegation_order WHERE member_id=#{memberId} and status = 1 FOR UPDATE")
    DelegationOrder selectByMemberIdForUpdate(String memberId);

}
