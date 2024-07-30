package org.uu.wallet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.TaskCollectionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.math.BigDecimal;

/**
 * <p>
 * 会员领取任务记录 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-03-18
 */
@Mapper
public interface TaskCollectionRecordMapper extends BaseMapper<TaskCollectionRecord> {

    Long getFinishNum();

    Long getReceiveNum();

    BigDecimal getRewardAmount();
}
