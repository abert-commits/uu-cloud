package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.uu.common.pay.dto.AppVersionDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * APP版本管理 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-04-20
 */
@Mapper
public interface AppVersionManagerMapper extends BaseMapper<AppVersionDTO> {

}
