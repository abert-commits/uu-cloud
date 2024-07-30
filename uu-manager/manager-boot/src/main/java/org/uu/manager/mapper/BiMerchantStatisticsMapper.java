package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.uu.manager.entity.BiMerchantStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 商户统计报表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-03-09
 */
@Mapper
public interface BiMerchantStatisticsMapper extends BaseMapper<BiMerchantStatistics> {

    void deleteByDateTime(String dateStr);
}
