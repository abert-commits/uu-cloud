package org.uu.wallet.service;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskRulesContentDTO;
import org.uu.common.pay.req.TaskRulesContentReq;
import org.uu.wallet.entity.TaskRulesContent;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.vo.MemberInformationVo;
import org.uu.wallet.vo.TaskRuleDetailsVo;

/**
 * <p>
 * 任务规则内容 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-20
 */
public interface ITaskRulesContentService extends IService<TaskRulesContent> {
    RestResult<TaskRulesContentDTO>  detail();
    RestResult<?>  updateContent(TaskRulesContentReq req);

    /**
     * 获取任务规则详情
     * @return {@link RestResult}<{@link MemberInformationVo}>
     */
    RestResult<TaskRuleDetailsVo> getTaskRuleDetails();

}
