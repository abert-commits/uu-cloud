package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskCollectionRecordDTO;
import org.uu.common.pay.req.TaskCollectionRecordReq;
import org.uu.wallet.entity.MemberTaskStatus;
import org.uu.wallet.entity.TaskCollectionRecord;
import org.uu.wallet.req.ClaimTaskRewardReq;
import org.uu.wallet.req.TaskCollectionRecordPageReq;
import org.uu.wallet.vo.PrizeWinnersVo;
import org.uu.wallet.vo.TaskCollectionRecordListVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 会员领取任务记录 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-18
 */
public interface ITaskCollectionRecordService extends IService<TaskCollectionRecord> {

    PageReturn<TaskCollectionRecordDTO> listPage(TaskCollectionRecordReq req);

    /**
     * 前台-分页查询奖励明细
     * @param req
     * @return
     */
    RestResult<PageReturn<TaskCollectionRecordListVo>> getPageList(TaskCollectionRecordPageReq req);


    /**
     * 获取领奖会员列表
     *
     * @return {@link List}<{@link PrizeWinnersVo}>
     */
    List<PrizeWinnersVo> getPrizeWinnersList();

    TaskCollectionRecordDTO getStatisticsData();


    /**
     * 查看会员是否领取过任务奖励
     *
     * @param memberId 会员id
     * @param taskId   任务id
     * @return boolean
     */
    boolean checkTaskCompletedByMember(Long memberId, Long taskId);

}
