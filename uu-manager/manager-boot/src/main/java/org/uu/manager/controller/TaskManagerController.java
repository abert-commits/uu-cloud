package org.uu.manager.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskManagerDTO;
import org.uu.common.pay.req.TaskManagerIdReq;
import org.uu.common.pay.req.TaskManagerListReq;
import org.uu.common.pay.req.TaskManagerReq;
import org.uu.manager.api.TaskManagerClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 * @date 2024/3/19 9:47
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "任务管理控制器")
@RequestMapping(value = {"/api/v1/taskManagerAdmin", "/taskManagerAdmin"})
public class TaskManagerController {
    private final TaskManagerClient taskManagerClient;
    @PostMapping("/listPage")
    @ApiOperation(value = "获取任务管理列表")
    public RestResult<TaskManagerDTO> listPage(@RequestBody @ApiParam TaskManagerListReq req) {
        PageReturn<TaskManagerDTO> taskManagerDto = taskManagerClient.listPage(req);
        return RestResult.page(taskManagerDto);
    }

    @PostMapping("/taskDetail")
    @ApiOperation(value = "获取任务管理详情")
    public RestResult<TaskManagerDTO> getBannerById(@RequestBody @ApiParam TaskManagerIdReq req) {
        return taskManagerClient.taskDetail(req);
    }

    @PostMapping("/createTask")
    @ApiOperation(value = "新建任务")
    public RestResult<?> createTask(@RequestBody @ApiParam TaskManagerReq req) {
        return taskManagerClient.createTask(req);
    }

    @PostMapping("/deleteTask")
    @ApiOperation(value = "删除任务")
    public RestResult<?> deleteTask(@RequestBody @ApiParam TaskManagerIdReq req) {
        return taskManagerClient.deleteTask(req);
    }

    @PostMapping("/updateTask")
    @ApiOperation(value = "更新任务内容")
    public RestResult<?> updateTask(@RequestBody @ApiParam TaskManagerReq req) {
        return taskManagerClient.updateTask(req);
    }
}
