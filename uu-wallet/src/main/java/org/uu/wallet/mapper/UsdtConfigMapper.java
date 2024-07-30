package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.UsdtConfig;

/**
 * @author
 */
@Mapper
public interface UsdtConfigMapper extends BaseMapper<UsdtConfig> {

    /**
     * 匹配USDT收款信息 加上排他行锁
     *
     * @param networkProtocol
     * @return {@link UsdtConfig}
     */
    @Select("SELECT * FROM usdt_config WHERE network_protocol = #{networkProtocol} AND status = 1 FOR UPDATE")
    UsdtConfig selectUsdtConfigForUpdate(String networkProtocol);
}
