package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.RechargeTronDetail;

import java.math.BigDecimal;

/**
 * <p>
 * 钱包交易记录 Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@Mapper
public interface RechargeTronDetailMapper extends BaseMapper<RechargeTronDetail> {

    /**
     * 根据U地址 查询钱包交易记录 订单号为0 加上排他行锁
     *
     * @param toAddress
     * @return {@link RechargeTronDetail}
     */
    @Select("SELECT * FROM recharge_tron_detail WHERE to_address = #{toAddress} AND order_id = '0' LIMIT 1 FOR UPDATE")
    RechargeTronDetail selectOneDetailForUpdate(@Param("toAddress") String toAddress);

    @Select("SELECT * FROM recharge_tron_detail WHERE to_address = #{toAddress} AND amount = #{amount} AND order_id = '0' LIMIT 1 FOR UPDATE")
    RechargeTronDetail selectOneByToAddressAndAmountForUpdate(@Param("toAddress") String toAddress, @Param("amount")BigDecimal amount);
}
