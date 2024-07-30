package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.manager.entity.BiMerchantReconciliation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 商户对账报表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-03-06
 */
@Mapper
public interface BiMerchantReconciliationMapper extends BaseMapper<BiMerchantReconciliation> {

    void deleteByDateTime(@Param("dateTime")String dateStr);

    void updateByDateTime(@Param("vo")BiMerchantReconciliation biPaymentOrder);
}
