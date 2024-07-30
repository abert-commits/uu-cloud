package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.dto.TaskRulesContentDTO;
import org.uu.common.pay.req.TaskRulesContentReq;
import org.uu.wallet.entity.TaskRulesContent;
import org.uu.wallet.mapper.TaskRulesContentMapper;
import org.uu.wallet.service.ITaskRulesContentService;
import org.uu.wallet.vo.MemberInformationVo;
import org.uu.wallet.vo.TaskRuleDetailsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务规则内容 服务实现类
 * </p>
 *
 * @author
 * @since 2024-03-20
 */
@Service
@Slf4j
public class TaskRulesContentServiceImpl extends ServiceImpl<TaskRulesContentMapper, TaskRulesContent> implements ITaskRulesContentService {

    @Override
    public RestResult<TaskRulesContentDTO> detail() {
        TaskRulesContentDTO dto = new TaskRulesContentDTO();
        TaskRulesContent result = getContent();
        BeanUtils.copyProperties(result, dto);
        return RestResult.ok(dto);
    }

    @Override
    public RestResult<?> updateContent(TaskRulesContentReq req) {
        TaskRulesContent update = getContent();
        update.setTaskRulesContent(req.getTaskRulesContent());
        int i = baseMapper.updateById(update);
        return i == 0 ? RestResult.failed() : RestResult.ok();
    }

    /**
     * 获取任务规则详情
     * @return {@link RestResult}<{@link MemberInformationVo}>
     */
    @Override
    public RestResult<TaskRuleDetailsVo> getTaskRuleDetails() {

        TaskRulesContent taskRulesContent = getById(1);

        if (taskRulesContent == null) {
            log.error("获取任务规则详情失败, 任务规则内容为null");
            return RestResult.failure(ResultCode.CONTENT_NOT_FOUND);
        }

        TaskRuleDetailsVo taskRuleDetailsVo = new TaskRuleDetailsVo();

        taskRuleDetailsVo.setTaskRulesContent(taskRulesContent.getTaskRulesContent());

        return RestResult.ok(taskRuleDetailsVo);
    }

    private TaskRulesContent getContent(){
        return lambdaQuery().eq(TaskRulesContent::getId, 1).one();
    }
}
