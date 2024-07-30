package org.uu.wallet.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.TradeIpBlacklist;

/**
 * <p>
 * 交易IP黑名单表，用于存储不允许进行交易的IP地址 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-02-21
 */
@Mapper
public interface TradeIpBlacklistMapper extends BaseMapper<TradeIpBlacklist> {

}
