package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.manager.entity.BiMemberReconciliation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 会员对账报表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-03-06
 */
@Mapper
public interface BiMemberReconciliationMapper extends BaseMapper<BiMemberReconciliation> {

    void deleteByDateTime(@Param("dateTime")String dateStr);
}
