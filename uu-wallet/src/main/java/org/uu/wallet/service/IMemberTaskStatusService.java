package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberTaskStatus;
import org.uu.wallet.entity.TaskManager;

import java.util.List;

/**
 * <p>
 * 会员任务状态表, 记录会员完成任务和领取奖励的状态 服务类
 * </p>
 *
 * @author
 * @since 2024-03-22
 */
public interface IMemberTaskStatusService extends IService<MemberTaskStatus> {


    /**
     * 处理每日买入任务
     *
     * @param memberInfo
     * @param taskManager
     * @return boolean
     */
    boolean handleDailyBuyTask(MemberInfo memberInfo, TaskManager taskManager);


    /**
     * 处理每日卖出任务
     *
     * @param memberInfo
     * @param taskManager
     * @return boolean
     */
    boolean handleDailySellTask(MemberInfo memberInfo, TaskManager taskManager);

    /**
     * 查询会员当天所有任务状态列表
     *
     * @param memberId
     * @return
     */
    List<MemberTaskStatus> queryMemberTodayTaskStatus(Long memberId);


    /**
     * 检查是否完成了任务 并且未领取奖励
     *
     * @param memberInfo
     * @param taskManager
     * @return {@link MemberTaskStatus}
     */
    MemberTaskStatus checkTaskCompletedAndRewardUnclaimed(MemberInfo memberInfo, TaskManager taskManager);


    /**
     * 完成实名认证任务
     *
     * @param memberInfo
     */
    Boolean completeOnceTask(MemberInfo memberInfo, TaskManager taskManager);
}
