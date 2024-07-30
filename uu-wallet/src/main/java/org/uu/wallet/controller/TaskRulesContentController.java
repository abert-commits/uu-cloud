package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskRulesContentDTO;
import org.uu.common.pay.req.TaskRulesContentReq;
import org.uu.wallet.service.ITaskRulesContentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * <p>
 * 任务规则内容 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-03-20
 */
@RestController
@RequiredArgsConstructor
@Api("任务规则内容控制器")
@RequestMapping(value = {"/api/v1/taskRulesContent", "taskRulesContent"})
@ApiIgnore
public class TaskRulesContentController {
    private final ITaskRulesContentService taskRulesContentService;

    @PostMapping("/detail")
    @ApiOperation(value = "获取任务规则内容")
    public RestResult<TaskRulesContentDTO> detail() {
        return taskRulesContentService.detail();
    }

    @PostMapping("/updateContent")
    @ApiOperation(value = "更新任务规则内容")
    public RestResult<?> updateContent(@RequestBody @ApiParam TaskRulesContentReq req) {
        return taskRulesContentService.updateContent(req);
    }
}
