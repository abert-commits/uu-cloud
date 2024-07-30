package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.MerchantRatesConfig;

/**
 * <p>
 * 商户对应的代收、代付费率设置 Mapper 接口
 * </p>
 */
@Mapper
public interface MerchantRatesConfigMapper extends BaseMapper<MerchantRatesConfig> {

    @Select("SELECT * FROM merchant_rates_config WHERE merchant_code = #{merchantCode} and type = #{type} and pay_type = #{payType}")
    MerchantRatesConfig selectMerchantRateConfigByMerchantId(@Param("merchantCode") String merchantCode, @Param("type") Integer type, @Param("payType") String payType);

}
