package org.uu.manager.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskRulesContentDTO;
import org.uu.common.pay.req.TaskRulesContentReq;
import org.uu.manager.api.TaskRulesContentClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 * @date 2024/3/20 14:34
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "任务规则内容控制器")
@RequestMapping(value = {"/api/v1/taskRulesContentAdmin", "taskRulesContentAdmin"})
public class TaskRulesContentController {
    private final TaskRulesContentClient taskRulesContentClient;
    @PostMapping("/detail")
    @ApiOperation(value = "获取任务规则内容")
    public RestResult<TaskRulesContentDTO> detail() {
        return taskRulesContentClient.detail();
    }

    @PostMapping("/updateContent")
    @ApiOperation(value = "更新任务规则内容")
    public RestResult<?> updateContent(@RequestBody @ApiParam TaskRulesContentReq req) {
        return taskRulesContentClient.updateContent(req);
    }
}
