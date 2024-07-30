package org.uu.wallet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.MemberTaskStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 会员任务状态表, 记录会员完成任务和领取奖励的状态 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-03-22
 */
@Mapper
public interface MemberTaskStatusMapper extends BaseMapper<MemberTaskStatus> {

}
