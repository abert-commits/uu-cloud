package org.uu.wallet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.common.pay.dto.MemberManualLogDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 会员手动操作记录 Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-02-29
 */
@Mapper
public interface MemberManualLogMapper extends BaseMapper<MemberManualLogDTO> {


}
