package org.uu.wallet.service;


import org.uu.common.core.result.RestResult;
import org.uu.wallet.entity.TaskCollectionRecord;
import org.uu.wallet.vo.TaskCenterVo;

/**
 * 任务中心 服务类
 *
 * @author Simon
 * @date 2024/03/20
 */
public interface ITaskCenterService {

    /**
     * 获取任务中心页面数据
     *
     * @return {@link RestResult}<{@link TaskCenterVo}>
     */
    RestResult<TaskCenterVo> fetchTaskCenterDetails();


    /**
     * 查看会员是否完成过任务 true 已完成过该任务
     *
     * @param memberId
     * @param taskId
     * @return boolean
     */
    boolean hasMemberCompletedTask(Long memberId, Long taskId);


    /**
     * 完成任务处理
     * 1.将领取记录到redis
     * 2.将领取记录到mysql
     *
     * @param memberId
     * @param taskId
     * @return boolean
     */
    boolean tryCompleteTask(Long memberId, Long taskId, TaskCollectionRecord taskCollectionRecord);
}
