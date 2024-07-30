package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.TradeConfig;

/**
 * @author
 */
@Mapper
public interface TradeConfigMapper extends BaseMapper<TradeConfig> {
    /**
     * 查询配置信息 加上排他行锁
     *
     * @param id
     * @return {@link TradeConfig}
     */
    @Select("SELECT * FROM trade_config WHERE id = #{id} FOR UPDATE")
    TradeConfig selectTradeConfigForUpdate(Long id);
}
