package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.manager.entity.BiMemberStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 会员统计报表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-03-09
 */
@Mapper
public interface BiMemberStatisticsMapper extends BaseMapper<BiMemberStatistics> {

    void deleteByDateTime(@Param("dateTime")String dateStr);
}
